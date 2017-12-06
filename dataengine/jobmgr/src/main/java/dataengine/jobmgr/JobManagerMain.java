package dataengine.jobmgr;

import java.io.IOException;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dataengine.apis.CommunicationConsts;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;

@Slf4j
public class JobManagerMain {

  public static void main(String[] args) throws Exception {
    main((String)null);
  }
  public static void main(String brokerUrl) throws IOException, JMSException {
    log.info("Starting {}", JobManagerMain.class);
    Properties properties=new Properties();
//    PropertiesUtil.loadProperties("jobmgr.props", properties);
    if(brokerUrl!=null) {
      log.info("Setting brokerUrl={}", brokerUrl);
      properties.setProperty("brokerUrl", brokerUrl);
    }
    Connection connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(connection, properties);
    JobBoardModule.deployJobBoardVerticles(injector, CommunicationConsts.jobBoardBroadcastAddr);
    {
      Properties compProps = new Properties(properties);
      compProps.setProperty("_componentId", "getFromZookeeper-DepJob"); //FIXME
      JobBoardModule.deployDepJobService(injector, CommunicationConsts.depJobMgrBroadcastAddr, compProps);
    }
    connection.start();
  }

  static Injector createInjector(Connection connection, Properties properties) {
    return Guice.createInjector(
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Connection.class).toInstance(connection);
          }
        },
        new RpcClients4JobMgrModule(connection),
        new JobBoardModule(CommunicationConsts.jobBoardBroadcastAddr, connection)
        );
  }
}
