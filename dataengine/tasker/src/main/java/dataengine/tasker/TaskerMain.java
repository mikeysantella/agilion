package dataengine.tasker;

import java.io.IOException;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.JMSException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;
import net.deelam.zkbasedinit.ConfigReader;
import net.deelam.zkbasedinit.GModuleZooKeeper;

@Slf4j
public class TaskerMain {

  public static void main(String[] args) throws Exception {
    main((String) null);
  }

  public static void main(String brokerUrl) throws Exception {
    Properties properties=new Properties();
    PropertiesUtil.loadProperties("tasker.props", properties);
    if(brokerUrl!=null) {
      log.info("Setting brokerUrl={}", brokerUrl);
      properties.setProperty("brokerUrl", brokerUrl);
    }
    
    String propFile = "startup.props";
    Configuration config = ConfigReader.parseFile(propFile);
    //log.info("{}\n------", ConfigReader.toStringConfig(config, config.getKeys()));
    String zookeeperConnectStr=config.getString("ZOOKEEPER.CONNECT", "127.0.0.1:2181");

    main(zookeeperConnectStr, brokerUrl, properties, "depJobMgrBroadcastAMQ");
  }

  public static void main(String zookeeperConnectStr, String brokerUrl, Properties properties, String dispatcherRpcAddr) throws JMSException, ConfigurationException {
    log.info("Starting {}", TaskerMain.class);
    
    connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(zookeeperConnectStr, connection, properties);
    
    opsRegistry=OperationsRegistryModule.deployOperationsRegistry(injector);
    TaskerModule.deployTasker(injector);
    dispatcherListener = TaskerModule.deployDispatcherListener(injector, "/test/fromEclipse/startup/jobMgrType/copies"); //FIXME
    connection.start();
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
  
  static Injector createInjector(String zkConnectionString, Connection connection, Properties properties) {
    return Guice.createInjector(
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Connection.class).toInstance(connection);
          }
        },
        new GModuleZooKeeper(zkConnectionString, null),
        new RpcClients4TaskerModule(connection),
        new OperationsRegistryModule(),
        new TaskerModule(properties)
        );
  }

}
