package dataengine.main;

import java.util.Properties;
import javax.jms.JMSException;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.ConstantsAmq;
import net.deelam.coordworkers.AbstractCompConfig;
import net.deelam.zkbasedinit.ComponentConfigI;
import net.deelam.zkbasedinit.ComponentI;

@Slf4j
public class WorkersComponent implements ComponentI {
  
  @Getter
  private boolean running = true;

  @Delegate(types=ComponentConfigI.class)
  WorkersConfig config;

  static class WorkersConfig extends AbstractCompConfig {

    final String brokerUrl;
    final String dispatcherRpcAddr;
    final String jobBoardRpcAddr;
    final String newJobAvailableTopic;
//    int deliveryMode = DeliveryMode.NON_PERSISTENT;

    public WorkersConfig(Properties props) {
      super(props);
      brokerUrl = ConstantsAmq.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
      dispatcherRpcAddr = useRequiredProperty(props, "msgQ.dispatcherRpcAddr");
      jobBoardRpcAddr = useRequiredProperty(props, "msgQ.jobBoardRpcAddr");
      newJobAvailableTopic = useRequiredProperty(props, "msgT.newJobAvailable");
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
    log.error("Reinitializing component '{}' with: {}", getComponentId(), configMap);
    return false;
  }

  @Override
  public void stop() {
    log.info("Stopping component: {}", getComponentId());
    dataengine.workers.WorkerMain.shutdown();
    running = false;
  }

}
