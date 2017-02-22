package dataengine.sessions;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.deelam.graph.GrafTxn.tryAndCloseTxn;
import static net.deelam.graph.GrafTxn.tryCAndCloseTxn;
import static net.deelam.graph.GrafTxn.tryFAndCloseTxn;

import java.util.List;
import java.util.Map;

import com.tinkerpop.blueprints.TransactionalGraph;
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

  public JobFrame getJobFrame(String id) {
    return frameHelper.getVertexFrame(id, JobFrame.class);
  }

  static final String JOB_PARAMS_PROPPREFIX = "params.";
  static final String JOB_STATS_PROPPREFIX = "stats.";
  static int jobCounter = 0;

  @SuppressWarnings("unchecked")
  public void addJobNode(Job job) {
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
          jf.setLabel(job.getLabel());

        if (job.getType() != null)
          jf.setType(job.getType());
        if (job.getParams() != null)
          SessionDB_FrameHelper.saveMapAsProperties(job.getParams(),
              jf.asVertex(), JOB_PARAMS_PROPPREFIX);

        ((Map<String, String>) job.getInputDatasetIds()).forEach((String name, String nodeId) -> {
          DatasetFrame df = dsHelper.getDatasetFrame(nodeId);
          jf.addInputDataset(df);
        });

        ((Map<String, String>) job.getOutputDatasetIds()).forEach((String name, String nodeId) -> {
          DatasetFrame df = dsHelper.getDatasetFrame(nodeId);
          jf.addOutputDataset(df);
        });
      } else {
        throw new IllegalArgumentException("Job.id already exists: " + jobId);
      }
    });
  }


  public static Job toJob(JobFrame jf) {
    return new Job().id(jf.getNodeId())
        .label(jf.getLabel())
        .requestId(jf.getRequest().getNodeId())
        .type(jf.getType())
        .state(jf.getState())
        .progress(new Progress()
            .percent(jf.getProgress())
            .stats(SessionDB_FrameHelper.loadPropertiesAsMap(jf.asVertex(), JOB_STATS_PROPPREFIX))
            )
        .params(SessionDB_FrameHelper.loadPropertiesAsMap(jf.asVertex(), JOB_PARAMS_PROPPREFIX))
        .inputDatasetIds(SessionDB_DatasetHelper.toDatasetMap(jf.getInputDatasets()))
        .outputDatasetIds(SessionDB_DatasetHelper.toDatasetMap(jf.getOutputDatasets()));
  }

  public static List<Job> toJobs(Iterable<JobFrame> jobs) {
    return stream(jobs.spliterator(), false)
        .map(req -> toJob(req))
        .collect(toList());
  }

  ///

  public void updateJobState(String jobId, State state) {
    tryCAndCloseTxn(graph, graph -> getJobFrame(jobId).setState(state));
  }

  public void updateJobProgress(String jobId, Progress progress) {
    tryCAndCloseTxn(graph, graph -> {
      JobFrame jf = getJobFrame(jobId);
      jf.setProgress(progress.getPercent());
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
