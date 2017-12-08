package dataengine.main;

import java.util.Properties;
import javax.jms.JMSException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.Constants;
import net.deelam.coordworkers.AbstractCompConfig;
import net.deelam.zkbasedinit.ComponentI;

@Slf4j
public class WorkersComponent implements ComponentI {
  
  @Getter
  private boolean running = true;

  public String getComponentId() {
    return config.componentId();
  }

  WorkersConfig config;

  static class WorkersConfig extends AbstractCompConfig {

    final String brokerUrl;
    final String dispatcherRpcAddr;
    final String jobBoardRpcAddr;
//    final String jobFailedTopic;
//    final String getJobsTopic;
    final String newJobAvailableTopic;
//    final String pickedJobQueue;
//    int deliveryMode = DeliveryMode.NON_PERSISTENT;


    // populate and print remaining unused properties
    public WorkersConfig(Properties props) {
      super(props);
      brokerUrl = Constants.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
      dispatcherRpcAddr = useRequiredProperty(props, "msgQ.dispatcherRpcAddr");
      jobBoardRpcAddr = useRequiredProperty(props, "msgQ.jobBoardRpcAddr");
//      submitJobQueue = useRequiredProperty(props, "msgQ.submitJob");
//      jobDoneTopic = useRequiredProperty(props, "msgT.jobDone");
//      jobFailedTopic = useRequiredProperty(props, "msgT.jobFailed");
//      getJobsTopic = useRequiredProperty(props, "msgT.getJobs");
      newJobAvailableTopic = useRequiredProperty(props, "msgT.newJobAvailable");
//      pickedJobQueue = useProperty(props, "msgQ.pickedJob", availJobsTopic + ".pickedJob");
      checkRemainingProps(props);
    }

  }
  
  @Override
  public void start(Properties configMap) {
    config = new WorkersConfig(configMap);

    log.info("Starting {}", this);
    try {
      dataengine.workers.WorkerMain.main(config.brokerUrl, config.newJobAvailableTopic, config.dispatcherRpcAddr, config.jobBoardRpcAddr);
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
