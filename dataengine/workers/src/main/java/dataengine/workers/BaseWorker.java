package dataengine.workers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.apis.OperationConsts;
import dataengine.apis.OperationUtils;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.JobDTO;
import net.deelam.vertx.jobboard.ProgressState;
import net.deelam.vertx.jobboard.ProgressingDoer;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(fluent = true)
@Getter
@Slf4j
abstract class BaseWorker<T extends Job> implements Worker_I, ProgressingDoer {

  final RpcClientProvider<SessionsDB_I> sessDb;

  private final String jobType;

  private final String name;

  protected final Operation operation;
  protected final Map<String, OperationParam> opParamsMap;
  protected final List<OperationParam> requiredParams;

  protected BaseWorker(String jobType, RpcClientProvider<SessionsDB_I> sessDb) {
    this.jobType = jobType;
    this.sessDb=sessDb;
    name = this.getClass().getSimpleName() + "-" + System.currentTimeMillis();
    operation=initOperation();
    opParamsMap = OperationUtils.initMap(operation);
    requiredParams = OperationUtils.getRequiredParams(operation);
  }

  abstract protected Operation initOperation();
  
  protected final ProgressState state = new ProgressState();

  @Override
  public void accept(JobDTO jobDto) {
    state.starting(jobDto.getId(), jobDto);
    //state.getMetrics().put(JobListener_I.METRICS_KEY_JOB, job);
    @SuppressWarnings("unchecked")
    T job = (T) jobDto.getRequest();
    try {
      OperationUtils.checkForRequiredParams(requiredParams, job.getParams());
      if (doWork(job))
        state.done(jobDto);
      else
        state.failed("doWork() returned false for job=" + job);
    } catch (RuntimeException e) {
      state.failed(e);
      throw e;
    } catch (Throwable e) {
      state.failed(e);
      throw new RuntimeException(e);
    }
  }

  protected boolean doWork(T job) throws Exception {
    log.error("TODO: implement doWork(): {}", job);
    AtomicInteger metricInt=new AtomicInteger();
    state.getMetrics().put("DUMMY_METRIC", metricInt);
    int numSeconds = 30;
    for(int i=0; i<numSeconds; ++i){
      state.setPercent(i*(100/(numSeconds+1))).setMessage("DUMMY: status at percent="+i*18);
      metricInt.incrementAndGet();
      Thread.sleep(1000);
    }
    return true;
  }
  
  CompletableFuture<String> getPrevJobDatasetId(Job job) {
    String prevJobId = (String) job.getParams().get(OperationConsts.PREV_JOBID);
    return sessDb.rpc().getJob(prevJobId)
        .thenApply(prevJob -> (String) prevJob.getParams().get(OperationConsts.OUTPUT_URI));
  }
  
  @RequiredArgsConstructor
  static class SubjobFactory {
    final String jobListenerAddr;
    final int progressPollIntervalSeconds;

    JobDTO createJobDTO(String subjobId, String subjobType, Object request) {
      return new JobDTO(subjobId, subjobType, request)
          .progressAddr(jobListenerAddr, progressPollIntervalSeconds);
    }
  }

}
