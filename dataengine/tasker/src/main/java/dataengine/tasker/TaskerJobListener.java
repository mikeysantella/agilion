package dataengine.tasker;

import javax.inject.Inject;

import dataengine.api.Progress;
import dataengine.api.State;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.ProgressState;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class TaskerJobListener extends AbstractVerticle implements JobListener_I {
  
  final RpcClientProvider<SessionsDB_I> sessDb;

  @Getter
  @Setter
  int progressPollIntervalSeconds=2;

  @Getter
  final String eventBusAddress="JobListener-"+System.currentTimeMillis();

  public void updateJobState(String jobId, State state) {
    log.info("SERV: updateJobState: {} {}", jobId, state);
    // placeholder to do any checking
    if(state!=null)
      sessDb.rpc().updateJobState(jobId, state);
  }

  public void updateJobProgress(String jobId, Progress progress) {
    log.info("SERV: updateJobState: {} {}", jobId, progress);
    // placeholder to do any checking
    sessDb.rpc().updateJobProgress(jobId, progress);
  }

  @Override
  public void start() throws Exception {
    super.start();
    
    vertx.eventBus().consumer(eventBusAddress, msg -> {
      JsonObject progressMsgJO = (JsonObject) msg.body();
      ProgressState progressState = Json.decodeValue(progressMsgJO.toString(), ProgressState.class);
      //Job job=(Job) progressState.getMetrics().get(JobListener_I.METRICS_KEY_JOB);
//      progressState.getMetrics().remove(JobListener_I.METRICS_KEY_JOB);
      Progress progress=new Progress()
          .percent(progressState.getPercent())
          .stats(progressState.getMetrics());
      updateJobProgress(progressState.getJobId(), progress);
      
      
      State state=null;
      if(progressState.getPercent()>=100)
        state=State.COMPLETED;
      else if(progressState.getPercent()>0)
        state=State.RUNNING;
      else if(progressState.getPercent()<0)
        state=State.FAILED;
      updateJobState(progressState.getJobId(), state);
    });
  }
  
}
