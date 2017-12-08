package dataengine.jobmgr;

import java.io.IOException;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;

@Slf4j
public class JobManagerMain {

  public static void main(String[] args) throws Exception {
    main((String) null);
  }

  public static void main(String brokerUrl) throws IOException, JMSException {
    Properties properties = new Properties();
    properties.setProperty("_componentId", "getFromZookeeper-DepJob");
    // PropertiesUtil.loadProperties("jobmgr.props", properties);
    if (brokerUrl != null) {
      log.info("Setting brokerUrl={}", brokerUrl);
      properties.setProperty("brokerUrl", brokerUrl);
    }
    main(brokerUrl, properties, "depJobMgrBroadcastAMQ", "jobBoardBroadcastAMQ", "newJobAvailableTopic");
  }

  public static void main(String brokerUrl, Properties properties, String dispatcherRpcAddr, String jobBoardRpcAddr, String newJobAvailableTopic) throws JMSException {
    log.info("Starting {}", JobManagerMain.class);
    Connection connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(connection, jobBoardRpcAddr, newJobAvailableTopic);
    JobBoardModule.deployJobBoardVerticles(injector);
    JobBoardModule.deployDepJobService(injector, dispatcherRpcAddr);
    connection.start();
  }

  static Injector createInjector(Connection connection, String jobBoardRpcAddr, String newJobAvailableTopic) {
    return Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Connection.class).toInstance(connection);
      }
    }, new RpcClients4JobMgrModule(connection, jobBoardRpcAddr),
        new JobBoardModule(jobBoardRpcAddr,
            newJobAvailableTopic, connection));
  }
}
