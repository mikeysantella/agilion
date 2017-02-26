package dataengine.tasker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Inject;

import com.google.common.collect.Lists;

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
import dataengine.tasker.jobcreators.AddSourceDataset;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.DepJobService_I;
import net.deelam.vertx.jobboard.JobDTO;

@Slf4j
public class TaskerService implements Tasker_I, JobListener_I {

  final OperationsRegistryVerticle opsRegVert;

  final RpcClientProvider<SessionsDB_I> sessDb;
  final RpcClientProvider<DepJobService_I> jobDispatcher;

  @Inject
  TaskerService(Supplier<SessionsDB_I> sessionsDbF, Supplier<DepJobService_I> jobDispatcherF,
      OperationsRegistryVerticle opsRegVert) {
    sessDb = new RpcClientProvider<>(sessionsDbF);
    jobDispatcher = new RpcClientProvider<>(jobDispatcherF);
    this.opsRegVert = opsRegVert;
  }

  // map keyed on Operation.id in a Request
  Map<String, JobsCreator> jobsCreatorMap = new HashMap<>();

  public void addjobsCreator(String operationId, JobsCreator jc) {
    jobsCreatorMap.put(operationId, jc);
  }

  @Override
  public CompletableFuture<Request> submitRequest(Request req) {
    log.info("submitRequest: {}", req);
    if (!opsRegVert.getOperations().containsKey(req.getOperationId()))
      throw new IllegalArgumentException("Unknown operationId=" + req.getOperationId());

    CompletableFuture<Request> f = sessDb.rpc().addRequest(req).thenApply((Request addedReq) -> {
      return submitJobs(addedReq);
    });
    return f;
  }

  private Request submitJobs(Request addedReq) {
    log.debug("submitJobs for: {}", addedReq);
    // add job(s) to request and submit job(s)
    JobsCreator jc = jobsCreatorMap.get(addedReq.getOperationId());
    if (jc == null)
      throw new UnsupportedOperationException("No JobsCreator found for " + addedReq.getOperationId());

    List<JobEntry> jobs = jc.createFrom(addedReq);

    CompletableFuture<Boolean> addJobsChainF = CompletableFuture.completedFuture(true);
    for (JobEntry jobE : jobs) {
      log.info("Adding new job "+jobE.job().getId());
      // add job to sessionsDB and submit to jobDispatcher
      addJobsChainF=addJobsChainF.thenCompose((isAdded) -> {
        if (isAdded)
          return addJob(jobE.job, jobE.inputJobIds);
        else
          throw new IllegalStateException("Previous job was not added!  Not continuing to add job: "+jobE.job.getId());
      });
    }
    return addedReq;
  }

  @Override
  public CompletableFuture<Void> refreshJobsCreators() {
    log.info("refreshJobsCreators()");
    return CompletableFuture.runAsync(() -> {
      addJobsCreators();
    });
  }

  private void addJobsCreators() {
    Map<String, Operation> currOperations = opsRegVert.getOperations();
    List<JobsCreator> jobCreators = Lists.newArrayList(
        new AddSourceDataset(currOperations));
    jobCreators.forEach(jc -> {
      Operation operation = jc.getOperation();
      currOperations.put(operation.getId(), operation);
      addjobsCreator(operation.getId(), jc);
    });
  }

  ///


  @Override
  public void updateJobState(String jobId, State state) {
    log.info("updateJobState: {} {}", jobId, state);
    // placeholder to do any checking
    sessDb.rpc().updateJobState(jobId, state);
  }

  @Override
  public void updateJobProgress(String jobId, Progress progress) {
    log.info("updateJobState: {} {}", jobId, progress);
    // placeholder to do any checking
    sessDb.rpc().updateJobProgress(jobId, progress);
  }

  @Override
  public CompletableFuture<Boolean> addJob(Job job, String[] inputJobIds) {
    log.info("addJob {} with inputs={}", job.getId(), Arrays.toString(inputJobIds));
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
