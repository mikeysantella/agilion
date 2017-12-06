package dataengine.tasker;

import java.util.Properties;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import dataengine.api.Progress;
import dataengine.api.State;
import dataengine.apis.DepJobService_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.Constants;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.KryoSerDe;
import net.deelam.coordworkers.AbstractCompConfig;
import net.deelam.vertx.jobboard.ProgressState;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class TaskerJobListener extends AbstractVerticle implements JobListener_I {
  
  private final Connection connection;
  final RpcClientProvider<SessionsDB_I> sessDb;
  
  @Getter
  final RpcClientProvider<DepJobService_I> jobDispatcher;

  @Getter
  @Setter
  int progressPollIntervalSeconds=2;

  @Getter
  final String eventBusAddress="JobListener-"+nextId();

  void updateJobState(String jobId, State state) {
    log.info("updateJobState: {} {}", jobId, state);
    // placeholder to do any checking
    if(state!=null)
      sessDb.rpc().updateJobState(jobId, state);
  }

  void updateJobProgress(String jobId, Progress progress) {
    log.info("updateJobProgress: {} {}", jobId, progress);
    // placeholder to do any checking
    sessDb.rpc().updateJobProgress(jobId, progress);
  }

  @Override
  public void start() throws Exception {
    super.start();
    
    vertx.eventBus().consumer(eventBusAddress, msg -> {
      JsonObject progressMsgJO = (JsonObject) msg.body();
      ProgressState progressState = Json.decodeValue(progressMsgJO.toString(), ProgressState.class);
      onProgressState(progressState);
    });
    
  }
  
  public void start(Properties configMap){
    config=new TaskerJobListenerConfig(configMap);
    try {
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      listenToJobStateMsgs(session, eventBusAddress/*config.jobStatusTopic*/);
    } catch (JMSException e) {
      throw new IllegalStateException("When registering to JMS service", e);
    }
  }

  private State onProgressState(ProgressState progressState) {
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
    return state;
  }
  
  
  private static int privateTaskerJobListenerIdCounter=0;
  @Synchronized
  static int nextId() {
    return ++privateTaskerJobListenerIdCounter;
  }

  Session session;
  TaskerJobListenerConfig config;

  class TaskerJobListenerConfig extends AbstractCompConfig {

//    final String brokerUrl;
//    final String jobStatusTopic;
    int deliveryMode = DeliveryMode.NON_PERSISTENT;


    // populate and print remaining unused properties
    public TaskerJobListenerConfig(Properties props) {
      super(props);
//      brokerUrl = Constants.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
//      jobStatusTopic = useRequiredProperty(props, "msgT.jobStatus");
      checkRemainingProps(props);
    }

  }
  
  void listenToJobStateMsgs(Session session, String stateTopicName) {
    final KryoSerDe serde = new KryoSerDe(session);

    if (stateTopicName != null)
      MQClient.createTopicConsumer(session, stateTopicName, msg -> {
        String jobId = msg.getStringProperty(ProgressState.JOBID_KEY);
        if (msg instanceof BytesMessage) {
          ProgressState progState=serde.readObject((BytesMessage) msg);
          log.info("Job statusMessage received: {}", progState);
          State state=onProgressState(progState);
          if (state != null)
            switch (state) {
              case COMPLETED:
                jobDispatcher.rpc().handleJobCompleted(jobId);
                break;
              case FAILED:
                jobDispatcher.rpc().handleJobFailed(jobId);
                break;
              case CANCELLED:
                break;
              default:
                break;
            }
        } else {
          log.warn("Expecting BytesMessage but got {}", msg);
        }
      });
  }
  
}
