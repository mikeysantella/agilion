package dataengine.tasker;

import java.io.IOException;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.AmqComponentSubscriber;
import net.deelam.utils.PropertiesUtil;
import net.deelam.zkbasedinit.ConstantsZk;
import net.deelam.zkbasedinit.GModuleZooKeeper;
import net.deelam.zkbasedinit.ZkComponentStarterI;

@Slf4j
public class TaskerMain {

  public static void main(String[] args) throws Exception {
    main((String) null);
  }

  public static void main(String brokerUrl) throws Exception {
//    Properties properties=new Properties();
//    PropertiesUtil.loadProperties("tasker.props", properties);
//    if(brokerUrl!=null) {
//      log.info("Setting brokerUrl={}", brokerUrl);
//      properties.setProperty("brokerUrl", brokerUrl);
//    }
    
    String propFile = "startup.props";
    Properties properties=new Properties();
    PropertiesUtil.loadProperties(propFile, properties);

    String zkConnectionString=properties.getProperty(ConstantsZk.ZOOKEEPER_CONNECT);
    String zkStartupPathHome=properties.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
    Properties configMap=new Properties();
    main(zkConnectionString, zkStartupPathHome, brokerUrl, null, "jobMgrType", configMap, DeliveryMode.NON_PERSISTENT);
  }

  public static void main(String zookeeperConnectStr, String zkStartupPath, String brokerUrl, String jobCreatorsString, 
      String dispatcherComponentType, Properties configMap, int deliveryMode) throws JMSException, ConfigurationException {
    log.info("Starting {}", TaskerMain.class);
    
    connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(zookeeperConnectStr, connection, jobCreatorsString, configMap, deliveryMode);
    
    opsRegistry=OperationsRegistryModule.deployOperationsRegistry(injector, deliveryMode);
    TaskerModule.deployTasker(injector, deliveryMode);
    dispatcherListener = TaskerModule.deployDispatcherListener(injector, zkStartupPath+dispatcherComponentType+ZkComponentStarterI.COPIES_SUBPATH);
  }

  private static OperationsRegistry opsRegistry;
  private static DispatcherComponentListener dispatcherListener;
  private static Connection connection;
  public static void shutdown() {
    try {
      opsRegistry.shutdown();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    try {
      dispatcherListener.shutdown();
    } catch (IOException e) {
      throw new IllegalStateException("When shutting down dispatcherListener", e);
    }
    try {
      connection.close();
    } catch (JMSException e) {
      throw new IllegalStateException(e);
    }
  }
  
  static Injector createInjector(String zkConnectionString, Connection connection, String jobCreatorsString, 
      Properties configMap, int deliveryMode) {
    return Guice.createInjector(
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Connection.class).toInstance(connection);
          }
        },
        new GModuleZooKeeper(zkConnectionString, null),
        new RpcClients4TaskerModule(connection, deliveryMode),
        new OperationsRegistryModule(deliveryMode),
        new TaskerModule(configMap, jobCreatorsString)
        );
  }

}
