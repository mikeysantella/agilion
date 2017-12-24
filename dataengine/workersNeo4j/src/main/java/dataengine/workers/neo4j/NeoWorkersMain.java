package dataengine.workers.neo4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dataengine.workers.BaseWorker;
import dataengine.workers.BaseWorkerModule;
import dataengine.workers.BaseWorkerModule.DeployedJobConsumerFactory;
import dataengine.workers.JobConsumer;
import dataengine.workers.OperationsSubscriberModule;
import dataengine.workers.RpcClients4WorkerModule;
import dataengine.workers.neo4j.CsvToNeoWorker.Factory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoWorkersMain {

  public static void main(String[] args) throws Exception {
    main((String) null);
  }

  public static void main(String brokerUrl) throws IOException, JMSException {
    log.info("Starting {}", NeoWorkersMain.class);
    Properties properties = new Properties();
    PropertiesUtil.loadProperties("workers.props", properties);
    if (brokerUrl != null) {
      log.info("Setting brokerUrl={}", brokerUrl);
      properties.setProperty("brokerUrl", brokerUrl);
    }
    String newJobAvailableTopic =
        properties.getProperty("newJobAvailableTopic", "newJobAvailableTopic");
    String dispatcherRpcAddr = properties.getProperty("dispatcherRpcAddr", "depJobMgrBroadcastAMQ");
    String jobBoardRpcAddr = properties.getProperty("jobBoardRpcAddr", "jobBoardBroadcastAMQ");
    Properties domainProps = PropertiesUtil.loadProperties("tide.props");
    main(brokerUrl, newJobAvailableTopic, dispatcherRpcAddr, jobBoardRpcAddr, domainProps);
  }

  public static void main(String brokerUrl, String newJobAvailableTopic, String dispatcherRpcAddr,
      String jobBoardRpcAddr, Properties domainProps) throws JMSException {
    connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(connection, dispatcherRpcAddr, jobBoardRpcAddr);
    DeployedJobConsumerFactory jcFactory =
        injector.getInstance(BaseWorkerModule.DeployedJobConsumerFactory.class);

    Factory csv2NeoWorkerFactory=injector.getInstance(CsvToNeoWorker.Factory.class);
    BaseWorker<?>[] hiddenWorkers = {
        csv2NeoWorkerFactory.create(domainProps)
    };

    BaseWorker<?>[] workers = {
        injector.getInstance(NeoExporterWorker.class)
    };

    jConsumers = new ArrayList<>(hiddenWorkers.length + workers.length);
    for (BaseWorker<?> worker : hiddenWorkers) {
      jConsumers.add(jcFactory.create(worker, newJobAvailableTopic));
    }
    for (BaseWorker<?> worker : workers) {
      OperationsSubscriberModule.deployOperationsSubscriber(connection, worker);
      jConsumers.add(jcFactory.create(worker, newJobAvailableTopic));
    }
  }

  private static List<JobConsumer> jConsumers;
  private static Connection connection;

  public static void shutdown() {
    jConsumers.forEach(JobConsumer::shutdown);
    try {
      connection.close();
    } catch (JMSException e) {
      throw new IllegalStateException(e);
    }
  }

  static Injector createInjector(Connection connection, String dispatcherRpcAddr,
      String jobBoardRpcAddr) {
    return Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Connection.class).toInstance(connection);
        }
      }, 
      new RpcClients4WorkerModule(connection, dispatcherRpcAddr, jobBoardRpcAddr),
      new OperationsSubscriberModule(), 
      new BaseWorkerModule());
  }
}
