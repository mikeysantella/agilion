package dataengine.tasker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.Progress;
import dataengine.api.Request;
import dataengine.api.State;
import dataengine.apis.JobListener_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import dataengine.tasker.JobsCreator.JobEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.DepJobService_I;
import net.deelam.vertx.jobboard.JobDTO;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class TaskerService implements Tasker_I, JobListener_I {

  final OperationsRegistryVerticle opsRegVert;

  final RpcClientProvider<SessionsDB_I> sessDb;
  final RpcClientProvider<DepJobService_I> jobDispatcher;

  // map keyed on Operation.id in a Request
  Map<String, JobsCreator> jobsCreatorMap = new ConcurrentHashMap<>();

  @Override
  public CompletableFuture<Request> submitRequest(Request req) {
    log.info("SERV: submitRequest: {}", req);
    String reqOperationId = req.getOperationId();
    if (!opsRegVert.getOperations().containsKey(reqOperationId))
      throw new IllegalArgumentException("Unknown operationId=" + reqOperationId);

    JobsCreator jc = jobsCreatorMap.get(reqOperationId);
    if (jc == null)
      throw new UnsupportedOperationException("No JobsCreator found for " + reqOperationId);

    jc.checkValidity(req); // throw exception
      
    CompletableFuture<Request> f = sessDb.rpc().addRequest(req)
        .thenApply((addedReq) ->submitJobs(addedReq, jc));
    return f;
  }

  private Request submitJobs(Request addedReq, JobsCreator jc) {
    log.info("  submitJobs for: {}", addedReq);
    // add job(s) to request and submit job(s)
    List<JobEntry> jobs = jc.createFrom(addedReq);

    CompletableFuture<Boolean> addJobsChainF = CompletableFuture.completedFuture(true);
    for (JobEntry jobE : jobs) {
      log.info("    adding new job " + jobE.job().getId());
      // add job to sessionsDB and submit to jobDispatcher
      addJobsChainF = addJobsChainF.thenCompose((isAdded) -> {
        if (isAdded)
          return addJob(jobE.job, jobE.inputJobIds);
        else
          throw new IllegalStateException(
              "Previous job was not added!  Not continuing to add job: " + jobE.job.getId());
      });
    }
    return addedReq;
  }

  private final List<JobsCreator> jobCreators; 

  @Override
  public CompletableFuture<Void> refreshJobsCreators() {
    log.info("SERV: refreshJobsCreators()");
    return CompletableFuture.runAsync(() -> {
      Map<String, Operation> currOps = opsRegVert.getOperations(); // TODO: 2: replace with RPC call and .thenApply
      
      jobCreators.forEach(jc -> {
        jc.update(currOps);
        JobsCreator oldJc = jobsCreatorMap.put(jc.getOperation().getId(), jc);
        if(oldJc!=null && oldJc!=jc)
          log.warn("Replaced JobsCreator old={} with updated={}", oldJc, jc);
        opsRegVert.mergeOperation(jc.getOperation());
      });
    });
  }

//  @Override
//  public CompletableFuture<Collection<Operation>> getJobCreatorsOperations() {
//    return CompletableFuture.completedFuture(
//        getJobCreators().stream().map(JobsCreator::getOperation).collect(toList()));
//  }

  ///


  @Override
  public void updateJobState(String jobId, State state) {
    log.info("SERV: updateJobState: {} {}", jobId, state);
    // placeholder to do any checking
    sessDb.rpc().updateJobState(jobId, state);
  }

  @Override
  public void updateJobProgress(String jobId, Progress progress) {
    log.info("SERV: updateJobState: {} {}", jobId, progress);
    // placeholder to do any checking
    sessDb.rpc().updateJobProgress(jobId, progress);
  }

  @Override
  public CompletableFuture<Boolean> addJob(Job job, String[] inputJobIds) {
    log.info("SERV: addJob {} with inputs={}", job.getId(), Arrays.toString(inputJobIds));
    // add job to sessionsDB
    CompletableFuture<Job> addJobToSessDB = sessDb.rpc().addJob(job);
    return addJobToSessDB.thenCompose((sessDbJob) -> {
      // submit job
      log.info("Submitting job={} to jobDispatcher", job.getId());
      JobDTO jobDto = new JobDTO(sessDbJob.getId(), sessDbJob.getType())
          .setRequest(sessDbJob)
          .setRequesterAddr(VerticleConsts.jobListenerBroadcastAddr);
      CompletableFuture<Boolean> submitJob = jobDispatcher.rpc().addDepJob(jobDto, inputJobIds);
      log.debug("Added and dispatched job={}", sessDbJob);
      return submitJob;
    });
  }

}
