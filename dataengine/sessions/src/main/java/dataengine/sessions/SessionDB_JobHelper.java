package dataengine.sessions;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.deelam.graph.GrafTxn.tryAndCloseTxn;
import static net.deelam.graph.GrafTxn.tryCAndCloseTxn;
import static net.deelam.graph.GrafTxn.tryFAndCloseTxn;
import static net.deelam.graph.GrafTxn.tryOn;

import java.util.List;
import java.util.Map;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedTransactionalGraph;

import dataengine.api.Job;
import dataengine.api.Progress;
import dataengine.api.State;
import dataengine.sessions.frames.DatasetFrame;
import dataengine.sessions.frames.JobFrame;
import dataengine.sessions.frames.RequestFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class SessionDB_JobHelper {

  private final FramedTransactionalGraph<TransactionalGraph> graph;
  private final SessionDB_DatasetHelper dsHelper;
  private final SessionDB_RequestHelper reqHelper;
  private final SessionDB_FrameHelper frameHelper;

  public boolean hasJob(String id) {
    return frameHelper.hasFrame(id, JobFrame.TYPE_VALUE);
  }
  
  public JobFrame getJobFrame(String id) {
    return frameHelper.getVertexFrame(id, JobFrame.class);
  }

  static final String JOB_PARAMS_PROPPREFIX = "params.";
  static final String JOB_STATS_PROPPREFIX = "stats.";
  static int jobCounter = 0;

  @SuppressWarnings("unchecked")
  public void addJobNode(Job job, String[] inputJobIds) {
    log.debug("addJobNode: {}", job.getId());
    tryAndCloseTxn(graph, graph -> {
      String reqId = job.getRequestId();
      RequestFrame rf = reqHelper.getRequestFrame(reqId);
      String jobId = job.getId();
      JobFrame existingJf = graph.getVertex(jobId, JobFrame.class);
      if (existingJf == null) {
        final JobFrame jf = graph.addVertex(jobId, JobFrame.class);
        rf.addJob(jf);

        if (job.getLabel() == null)
          jf.setLabel("job " + (++jobCounter));
        else
          jf.setLabel(SessionDB_FrameHelper.checkLabel(job.getLabel()));
        
        // Add createdTime so we can order the jobs
        if (job.getCreatedTime() != null)
          jf.setCreatedDate((job.getCreatedTime()));
        
        // add edge between jobs to model dependencies, matching DepJobService
        if(inputJobIds!=null)
          for(String inputJobId:inputJobIds)
            addJobDependency(jf, inputJobId);
        
        if (job.getType() != null)
          jf.setType(job.getType());
        if (job.getParams() != null)
          SessionDB_FrameHelper.saveMapAsProperties(job.getParams(),
              jf.asVertex(), JOB_PARAMS_PROPPREFIX);

        if(job.getInputDatasetIds()!=null)
          ((Map<String, String>) job.getInputDatasetIds()).forEach((String name, String nodeId) -> {
            DatasetFrame df = dsHelper.getDatasetFrame(nodeId);
            jf.addInputDataset(df);
          });

        if(job.getOutputDatasetIds()!=null)
          ((Map<String, String>) job.getOutputDatasetIds()).forEach((String name, String nodeId) -> {
            DatasetFrame df = dsHelper.getDatasetFrame(nodeId);
            jf.addOutputDataset(df);
          });
      } else {
        throw new IllegalArgumentException("Job.id already exists: " + jobId);
      }
    });
  }
  
  public void setJobParam(String jobId, String key, Object value){
    JobFrame existingJf = graph.getVertex(jobId, JobFrame.class);
    SessionDB_FrameHelper.setVertexProperty(existingJf.asVertex(), JOB_PARAMS_PROPPREFIX, key, value);
  }

  public void addJobDependency(String jobId, String inputJobId) {
    log.debug("addJobDependency: {} {}", jobId, inputJobId);
    tryCAndCloseTxn(graph, graph -> {
      JobFrame jf = frameHelper.getVertexFrame(jobId, JobFrame.class);
      addJobDependency(jf, inputJobId);
    });
  }
  
  private void addJobDependency(JobFrame jf, String inputJobId) {
    tryOn(graph, graph -> {
      JobFrame inputJF = frameHelper.getVertexFrame(inputJobId, JobFrame.class);
      jf.addInputJob(inputJF);
    });
  }
  
  public List<JobFrame> getInputJobs(String jobId) {
    log.debug("getInputJobs: {}", jobId);
    return tryFAndCloseTxn(graph, graph -> {
      JobFrame jf = frameHelper.getVertexFrame(jobId, JobFrame.class);
      return stream( jf.getInputJobs().spliterator(),false)
        .sorted(SessionDB_FrameHelper.createdTimeComparator).collect(toList());
    });
  }
  
  public List<JobFrame> getOutputJobs(String jobId) {
    log.debug("getOutputJobs: {}", jobId);
    return tryFAndCloseTxn(graph, graph -> {
      JobFrame jf = frameHelper.getVertexFrame(jobId, JobFrame.class);
      return stream( jf.getOutputJobs().spliterator(),false)
        .sorted(SessionDB_FrameHelper.createdTimeComparator).collect(toList());
    });
  }
  
  public static Job toJob(JobFrame jf) {
    log.debug("toJob: {}", jf);
    return new Job().id(jf.getNodeId())
        .label(jf.getLabel())
        .requestId(jf.getRequest().getNodeId())
        .type(jf.getType())
        .createdTime(jf.getCreatedDate())
        .state(jf.getState())
        .progress(toProgress(jf.getProgress(), jf.asVertex(), JOB_STATS_PROPPREFIX))
        .params(SessionDB_FrameHelper.loadPropertiesAsMap(jf.asVertex(), JOB_PARAMS_PROPPREFIX))
        .inputDatasetIds(SessionDB_DatasetHelper.toDatasetMap(jf.getInputDatasets()))
        .outputDatasetIds(SessionDB_DatasetHelper.toDatasetMap(jf.getOutputDatasets()));
  }

  private static Progress toProgress(int progress, Vertex asVertex, String propPrefix) {
    return new Progress()
        .percent(progress)
        .stats(SessionDB_FrameHelper.loadPropertiesAsMap(asVertex, propPrefix));
  }

  public static List<Job> toJobs(Iterable<JobFrame> jobs) {
    return stream(jobs.spliterator(),false)
        .sorted(SessionDB_FrameHelper.createdTimeComparator)
        .map(req -> toJob(req))
        .collect(toList());
  }

  ///

  public void updateJobState(String jobId, State state) {
    tryCAndCloseTxn(graph, graph -> {
      JobFrame jf = getJobFrame(jobId);
      if(jf.getState().equals(state))
        return;
      // check state transition is valid in case msg received out of order
      switch(state){
        case CREATED:
          log.warn("For {}, not expecting state={} from current state={}; IGNORING", jobId, state, jf.getState());
          break;
        case RUNNING:
          switch(jf.getState()){
            case CREATED:
              jf.setState(state);
              break;
            default:
              log.warn("For {}, not expecting state={} from current state={}; IGNORING", jobId, state, jf.getState());
              break;
          }
          break;
        case CANCELLED:
          switch(jf.getState()){
            case CREATED:
            case RUNNING:
              jf.setState(state);
              break;
            default:
              log.warn("For {}, not expecting state={} from current state={}; IGNORING", jobId, state, jf.getState());
              break;
          }
          break;
        case FAILED:
          switch(jf.getState()){
            case CREATED:
            case RUNNING:
              jf.setState(state);
              break;
            default:
              log.warn("For {}, not expecting state={} from current state={}; IGNORING", jobId, state, jf.getState());
              break;
          }
          break;
        case COMPLETED:
          switch(jf.getState()){
            case CREATED:
            case RUNNING:
              jf.setState(state);
              break;
            default:
              log.warn("For {}, not expecting state={} from current state={}; IGNORING", jobId, state, jf.getState());
              break;
          }
          break;
        default:
          log.error("For {}, state={} doesn't match any cases; IGNORING", jobId, state);
      }
    });
  }

  public void updateJobProgress(String jobId, Progress progress) {
    tryCAndCloseTxn(graph, graph -> {
      JobFrame jf = getJobFrame(jobId);
      // check percent is monotonically increasing in case msg received out of order
      if(jf.getProgress()<progress.getPercent())
        jf.setProgress(progress.getPercent());
      else if(jf.getProgress()>progress.getPercent()){
        log.warn("Ignoring setting a lower progress percent={} than existing={}", 
            progress.getPercent(), jf.getProgress());
      }
        
      if (progress.getStats() != null)
        SessionDB_FrameHelper.saveMapAsProperties(progress.getStats(),
            jf.asVertex(), JOB_STATS_PROPPREFIX);
    });
  }

  ////

  boolean isJobDone(String qualTaskIdOfInputGraph) {
    return tryFAndCloseTxn(graph, graph -> getJobFrame(qualTaskIdOfInputGraph).getState() == State.COMPLETED);
  }

  List<String> getOutputGraphUrisOfTask(String qualTaskIdOfInputGraph) {
    return tryFAndCloseTxn(graph, graph -> {
      return stream(getJobFrame(qualTaskIdOfInputGraph).getOutputDatasets().spliterator(), false)
          .map(ds -> ds.getUri()).collect(toList());
    });
  }

}
