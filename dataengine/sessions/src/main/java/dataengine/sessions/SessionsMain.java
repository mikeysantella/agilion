package dataengine.sessions;

import java.io.IOException;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;

@Slf4j
public class SessionsMain {
  
  public static void main(String[] args) throws Exception {
    main((String) null);
  }
  public static void main(String brokerUrl) throws IOException, JMSException {
    System.out.println("Starting "+SessionsMain.class.getSimpleName());
    Properties properties=new Properties();
    PropertiesUtil.loadProperties("sessions.props", properties);
    if(brokerUrl!=null) {
      log.info("Setting brokerUrl={}", brokerUrl);
      properties.setProperty("brokerUrl", brokerUrl);
    }
    main(brokerUrl, properties);
  }
  public static void main(String brokerUrl, Properties properties) throws JMSException {
    log.info("Starting {}", SessionsMain.class);
    connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(connection, properties);
    TinkerGraphSessionsDbModule.deploySessionDb(injector);
  }
  
  private static Connection connection;
  public static void shutdown() {
    try {
      connection.close();
    } catch (JMSException e) {
      throw new IllegalStateException(e);
    }
  }

  static Injector createInjector(Connection connection, Properties properties) {
    return Guice.createInjector(
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Connection.class).toInstance(connection);
          }
        },
        new TinkerGraphSessionsDbModule());
  }
}
