package dataengine.workers;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PythonWorkerModule extends AbstractModule {
  final Properties configMap;
  final int deliveryMode;

  @Override
  public void configure() {
    requireBinding(Connection.class);
    requireBinding(Properties.class);
  }

  @Provides
  PythonIngesterWorker createPythonIngesterWorker(RpcClientProvider<SessionsDB_I> sessDb,
      Connection connection, Properties props) throws JMSException {
    return new PythonIngesterWorker(sessDb, connection, deliveryMode, props);
  }

  @Provides
  PythonIngestExporterWorker createPythonIngestExporterWorker(
      RpcClientProvider<SessionsDB_I> sessDb, Connection connection) throws JMSException {
    return new PythonIngestExporterWorker(sessDb, connection, deliveryMode);
  }
}
