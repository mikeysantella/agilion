package dataengine.main;

import java.util.Properties;
import javax.jms.JMSException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.Constants;
import net.deelam.coordworkers.AbstractCompConfig;
import net.deelam.zkbasedinit.ComponentI;

@Slf4j
public class SessionsDbComponent implements ComponentI {
  
  @Getter
  private boolean running = true;

  public String getComponentId() {
    return config.componentId();
  }

  DataEngineConfig config;

  class DataEngineConfig extends AbstractCompConfig {

    final String brokerUrl;
//    final String submitJobQueue;
//    final String jobDoneTopic;
//    final String jobFailedTopic;
//    final String getJobsTopic;
//    final String availJobsTopic;
//    final String pickedJobQueue;
//    int deliveryMode = DeliveryMode.NON_PERSISTENT;


    // populate and print remaining unused properties
    public DataEngineConfig(Properties props) {
      super(props);
      brokerUrl = Constants.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
//      submitJobQueue = useRequiredProperty(props, "msgQ.submitJob");
//      jobDoneTopic = useRequiredProperty(props, "msgT.jobDone");
//      jobFailedTopic = useRequiredProperty(props, "msgT.jobFailed");
//      getJobsTopic = useRequiredProperty(props, "msgT.getJobs");
//      availJobsTopic = useRequiredProperty(props, "msgT.availJobs");
//      pickedJobQueue = useProperty(props, "msgQ.pickedJob", availJobsTopic + ".pickedJob");
      checkRemainingProps(props);
    }

  }
  
  @Override
  public void start(Properties configMap) {
    config = new DataEngineConfig(configMap);

    log.info("Starting {}", this);
    try {
      dataengine.sessions.SessionsMain.main(config.brokerUrl, configMap);
    } catch (JMSException e) {
      throw new IllegalStateException("While starting "+this, e);
    }
    running = true;
  }

  @Override
  public boolean reinit(Properties configMap) {
    log.info("Reinitializing component '{}' with: {}", getComponentId(), configMap);
    return false;
  }

  @Override
  public void stop() {
    log.info("Stopping component: {}", getComponentId());
    running = false;
  }

}
