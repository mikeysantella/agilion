package dataengine.workers.neo4j;

import java.io.IOException;
import java.util.Properties;
import javax.jms.JMSException;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.SettingsUtils;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.ConstantsAmq;
import net.deelam.coordworkers.AbstractCompConfig;
import net.deelam.utils.PropertiesUtil;
import net.deelam.zkbasedinit.ComponentConfigI;
import net.deelam.zkbasedinit.ComponentI;

@Slf4j
public class NeoWorkersComponent implements ComponentI {
  
  @Getter
  private boolean running = true;

  @Delegate(types=ComponentConfigI.class)
  NeoWorkersConfig config;

  static class NeoWorkersConfig extends AbstractCompConfig {

    final String brokerUrl;
    final String dispatcherRpcAddr;
    final String jobBoardRpcAddr;
    final String newJobAvailableTopic;
    final int deliveryMode;

    public NeoWorkersConfig(Properties props) {
      super(props);
      brokerUrl = ConstantsAmq.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
      dispatcherRpcAddr = useRequiredProperty(props, "msgQ.dispatcherRpcAddr");
      jobBoardRpcAddr = useRequiredProperty(props, "msgQ.jobBoardRpcAddr");
      newJobAvailableTopic = useRequiredProperty(props, "msgT.newJobAvailable");
      deliveryMode = SettingsUtils.parseMsgPersistenceProperty(
          useProperty(props, CommunicationConsts.MSG_PERSISTENCE, "PERSISTENT"));
      checkRemainingProps(props);
    }
  }
  
  @Override
  public void start(Properties configMap) {
    config = new NeoWorkersConfig(configMap);
    try {
      Properties domainProps = PropertiesUtil.loadProperties("tide.props");
      dataengine.workers.neo4j.NeoWorkersMain.main(config.brokerUrl, config.newJobAvailableTopic,
          config.dispatcherRpcAddr, config.jobBoardRpcAddr, domainProps, config.deliveryMode);
    } catch (JMSException | IOException e) {
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
    dataengine.workers.neo4j.NeoWorkersMain.shutdown();
    running = false;
  }

}
