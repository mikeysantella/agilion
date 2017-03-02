package dataengine.workers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import dataengine.api.Operation;
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
public class BaseWorker<T> implements Worker_I, ProgressingDoer {

  private final String jobType;

  private final String name;

  protected BaseWorker(String jobType) {
    this.jobType = jobType;
    name = this.getClass().getSimpleName() + "-" + System.currentTimeMillis();
  }

  protected final ProgressState state = new ProgressState();

  protected final Collection<Operation> operations = new ArrayList<>();

  @Override
  public void accept(JobDTO job) {
    state.starting(job.getId(), job);
    //state.getMetrics().put(JobListener_I.METRICS_KEY_JOB, job);
    @SuppressWarnings("unchecked")
    T req = (T) job.getRequest();
    try {
      if (doWork(req))
        state.done(job);
      else
        state.failed("doWork() returned false for request=" + req);
    } catch (RuntimeException e) {
      state.failed(e);
      throw e;
    } catch (Throwable e) {
      state.failed(e);
      throw new RuntimeException(e);
    }
  }

  protected boolean doWork(T req) throws Exception {
    log.error("TODO: implement doWork(): {}", req);
    AtomicInteger metricInt=new AtomicInteger();
    state.getMetrics().put("DUMMY_METRIC", metricInt);
    for(int i=0; i<5; ++i){
      state.setPercent(i*18).setMessage("DUMMY: status at percent="+i*18);
      metricInt.incrementAndGet();
      Thread.sleep(1000);
    }
    return true;
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
