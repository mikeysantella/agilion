package dataengine.tasker;

import java.util.Properties;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import com.google.inject.assistedinject.Assisted;
import dataengine.apis.ProgressState;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.KryoSerDe;
import net.deelam.coordworkers.AbstractCompConfig;

@Slf4j
//@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class TaskerJobListener implements JobListener_I {
  
  private final BiConsumer<Message,ProgressState> updateSessionDbHook;
  
  @Getter
  final String eventBusAddress;
  private static int privateTaskerJobListenerIdCounter=0;
  @Synchronized
  private static String nextId() {
    return ++privateTaskerJobListenerIdCounter + "-" + System.currentTimeMillis();
  }
  
  final Session session;
  final TaskerJobListenerConfig config;
  
  @Inject
  public TaskerJobListener(Connection connection, @Assisted Properties configMap, @Assisted BiConsumer<Message,ProgressState> handler) {
    eventBusAddress="JobListener-"+nextId();
    config=new TaskerJobListenerConfig(configMap);
    updateSessionDbHook = handler;
    try {
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      listenToJobStateMsgs(session, eventBusAddress);
    } catch (JMSException e) {
      throw new IllegalStateException("When registering to JMS service", e);
    }
  }

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
          updateSessionDbHook.accept(msg, progState);
        } else {
          log.warn("Expecting BytesMessage but got {}", msg);
        }
      });
  }
  
}
