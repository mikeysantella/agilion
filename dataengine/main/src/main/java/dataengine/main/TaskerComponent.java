package dataengine.main;

import java.util.Properties;
import javax.jms.JMSException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.Constants;
import net.deelam.coordworkers.AbstractCompConfig;
import net.deelam.zkbasedinit.ComponentConfigI;
import net.deelam.zkbasedinit.ComponentI;

@Slf4j
public class TaskerComponent implements ComponentI {
  
  @Getter
  private boolean running = true;

  @Delegate(types=ComponentConfigI.class)
  TaskerConfig config;

  class TaskerConfig extends AbstractCompConfig {
    final String brokerUrl;
    // int deliveryMode = DeliveryMode.NON_PERSISTENT;

    public TaskerConfig(Properties props) {
      super(props);
      brokerUrl = Constants.getTcpBrokerUrl(useRequiredRefProperty(props, "brokerUrl.ref"));
      checkRemainingProps(props);
    }
  }
  
  @Override
  public void start(Properties configMap) {
    config = new TaskerConfig(configMap);

    log.info("Starting {}", this);
    try {
      dataengine.tasker.TaskerMain.main(config.getZookeeperConnectString(), config.brokerUrl, configMap, "config.dispatcherRpcAddr");
    } catch (JMSException | ConfigurationException e) {
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
    dataengine.tasker.TaskerMain.shutdown();
    running = false;
  }

}
