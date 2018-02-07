package dataengine.main;

import java.util.Properties;
import javax.jms.JMSException;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.SettingsUtils;
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
    final int deliveryMode;

    public WorkersConfig(Properties props) {
      super(props);
      brokerUrl = ConstantsAmq.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
      dispatcherRpcAddr = useRequiredProperty(props, "msgQ.dispatcherRpcAddr");
      jobBoardRpcAddr = useRequiredProperty(props, "msgQ.jobBoardRpcAddr");
      newJobAvailableTopic = useRequiredProperty(props, "msgT.newJobAvailable");
      deliveryMode = SettingsUtils.parseMsgPersistenceProperty(
          useProperty(props, CommunicationConsts.MSG_PERSISTENCE, "PERSISTENT"));
      //Don't warn about worker-specific props: checkRemainingProps(props);
    }
  }
  
  @Override
  public void start(Properties configMap) {
    config = new WorkersConfig(configMap);
    try {
      dataengine.workers.WorkerMain.main(config.brokerUrl, 
          config.newJobAvailableTopic, config.dispatcherRpcAddr, config.jobBoardRpcAddr, //
          configMap, config.deliveryMode
          );
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
    dataengine.workers.WorkerMain.shutdown();
    running = false;
  }

}
