package dataengine.jobmgr;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
public class JobManagerMain {

  public static void main(String[] args) throws Exception {
    main(new CompletableFuture<>(), null);
  }
  public static void main(CompletableFuture<Vertx> vertxF, String brokerUrl) throws IOException, JMSException {
    log.info("Starting {}", JobManagerMain.class);
    Properties properties=new Properties();
//    PropertiesUtil.loadProperties("jobmgr.props", properties);
    if(brokerUrl!=null) {
      log.info("Setting brokerUrl={}", brokerUrl);
      properties.setProperty("brokerUrl", brokerUrl);
    }
    Connection connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(vertxF, connection, properties);
    JobBoardModule.deployJobBoardVerticles(injector, VerticleConsts.jobBoardBroadcastAddr);
    {
      Properties compProps = new Properties(properties);
      compProps.setProperty("_componentId", "getFromZookeeper-DepJob"); //FIXME
      JobBoardModule.deployDepJobService(injector, VerticleConsts.depJobMgrBroadcastAddr, compProps);
    }
    connection.start();
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF, Connection connection, Properties properties) {
    return Guice.createInjector(
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Connection.class).toInstance(connection);
          }
        },
        new ClusteredVertxInjectionModule(vertxF),
        new VertxRpcClients4JobMgrModule(vertxF, connection),
        new JobBoardModule(VerticleConsts.jobBoardBroadcastAddr, connection)
        );
  }
}
