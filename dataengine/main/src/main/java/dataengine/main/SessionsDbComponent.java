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
public class SessionsDbComponent implements ComponentI {
  
  @Getter
  private boolean running = true;

  @Delegate(types=ComponentConfigI.class)
  DataEngineConfig config;

  class DataEngineConfig extends AbstractCompConfig {

    final String brokerUrl;
    final int deliveryMode;

    // populate and print remaining unused properties
    public DataEngineConfig(Properties props) {
      super(props);
      brokerUrl = ConstantsAmq.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
      deliveryMode = SettingsUtils.parseMsgPersistenceProperty(
          useProperty(props, CommunicationConsts.MSG_PERSISTENCE, "PERSISTENT"));
      checkRemainingProps(props);
    }

  }
  
  @Override
  public void start(Properties configMap) {
    config = new DataEngineConfig(configMap);
    try {
      dataengine.sessions.SessionsMain.main(config.brokerUrl, configMap, config.deliveryMode);
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
    dataengine.sessions.SessionsMain.shutdown();
    running = false;
  }

}
