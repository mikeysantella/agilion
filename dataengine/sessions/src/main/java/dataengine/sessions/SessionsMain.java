package dataengine.sessions;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
public class SessionsMain {
  
  public static void main(String[] args) throws Exception {
    main(new CompletableFuture<>(), null);
  }
  public static void main(CompletableFuture<Vertx> vertxF, String brokerUrl) throws IOException, JMSException {
    System.out.println("Starting "+SessionsMain.class.getSimpleName());
    log.info("Starting {}", SessionsMain.class);
    Properties properties=new Properties();
    PropertiesUtil.loadProperties("sessions.props", properties);
    if(brokerUrl!=null) {
      log.info("Setting brokerUrl={}", brokerUrl);
      properties.setProperty("brokerUrl", brokerUrl);
    }
    Connection connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(vertxF, connection, properties);
    TinkerGraphSessionsDbModule.deploySessionDb(injector);
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
        new TinkerGraphSessionsDbModule());
  }
}
