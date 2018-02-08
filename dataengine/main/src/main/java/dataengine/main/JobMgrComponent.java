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
public class JobMgrComponent implements ComponentI {
  
  @Getter
  private boolean running = true;

  @Delegate(types=ComponentConfigI.class)
  DataEngineConfig config;

  class DataEngineConfig extends AbstractCompConfig {

    final String brokerUrl;
    final String dispatcherRpcAddr;
    final String jobBoardRpcAddr;
    final String newJobAvailableTopic;
//    int deliveryMode = DeliveryMode.NON_PERSISTENT;

    public DataEngineConfig(Properties props) {
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
    config = new DataEngineConfig(configMap);
    try {
      dataengine.jobmgr.JobManagerMain.main(config.brokerUrl, configMap, config.dispatcherRpcAddr, config.jobBoardRpcAddr, config.newJobAvailableTopic);
    } catch (JMSException e) {
      throw new IllegalStateException("While starting "+this, e);
    }
    running = true;
    log.info("COMP: Started {}", config.getComponentId());
  }

  @Override
  public boolean reinit(Properties configMap) {
    log.error("COMP: Reinitializing component '{}' with: {}", getComponentId(), configMap);
    return false;
  }

  @Override
  public void stop() {
    log.info("COMP: Stopping component: {}", getComponentId());
    dataengine.jobmgr.JobManagerMain.shutdown();
    running = false;
  }

}
