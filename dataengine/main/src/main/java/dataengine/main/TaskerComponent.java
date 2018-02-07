package dataengine.main;

import java.util.Properties;
import javax.jms.JMSException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.SettingsUtils;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.ConstantsAmq;
import net.deelam.coordworkers.AbstractCompConfig;
import net.deelam.zkbasedinit.ComponentConfigI;
import net.deelam.zkbasedinit.ComponentI;
import net.deelam.zkbasedinit.ConstantsZk;

@Slf4j
public class TaskerComponent implements ComponentI {
  
  @Getter
  private boolean running = true;

  @Delegate(types=ComponentConfigI.class)
  TaskerConfig config;

  class TaskerConfig extends AbstractCompConfig {
    final String brokerUrl;
    final String jobCreators;
    final String dispatcherComponentType;
    final String zkStartupPath;
    final int deliveryMode;

    public TaskerConfig(Properties props) {
      super(props);
      brokerUrl = ConstantsAmq.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
      jobCreators = useProperty(props, "jobCreators", null);
      dispatcherComponentType = useRequiredProperty(props, "dispatcherComponentType");
      zkStartupPath=System.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
      deliveryMode = SettingsUtils.parseMsgPersistenceProperty(
          useProperty(props, CommunicationConsts.MSG_PERSISTENCE, "PERSISTENT"));
      checkRemainingProps(props);
    }
  }
  
  @Override
  public void start(Properties configMap) {
    config = new TaskerConfig(configMap);
    try {
      dataengine.tasker.TaskerMain.main(config.getZookeeperConnectString(), config.zkStartupPath, config.brokerUrl, 
          config.jobCreators, config.dispatcherComponentType, config.deliveryMode);
    } catch (JMSException | ConfigurationException e) {
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
    dataengine.tasker.TaskerMain.shutdown();
    running = false;
  }

}
