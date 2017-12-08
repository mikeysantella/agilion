package dataengine.workers;

import java.io.IOException;
import java.util.Properties;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dataengine.workers.BaseWorkerModule.DeployedJobConsumerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class WorkerMain {

  public static void main(String[] args) throws Exception {
    main((String)null);
  }
  
  public static void main(String brokerUrl) throws IOException, JMSException {
    log.info("Starting {}", WorkerMain.class);
    Properties properties=new Properties();
    PropertiesUtil.loadProperties("workers.props", properties);
    if (brokerUrl != null) {
      log.info("Setting brokerUrl={}", brokerUrl);
      properties.setProperty("brokerUrl", brokerUrl);
    }
    String newJobAvailableTopic=properties.getProperty("newJobAvailableTopic", "newJobAvailableTopic");
    String dispatcherRpcAddr=properties.getProperty("dispatcherRpcAddr", "depJobMgrBroadcastAMQ");
    String jobBoardRpcAddr=properties.getProperty("jobBoardRpcAddr", "jobBoardBroadcastAMQ");
    main(brokerUrl, newJobAvailableTopic, dispatcherRpcAddr, jobBoardRpcAddr);
  }

  public static void main(String brokerUrl, String newJobAvailableTopic, String dispatcherRpcAddr, String jobBoardRpcAddr) throws JMSException {
    Connection connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(connection, dispatcherRpcAddr, jobBoardRpcAddr);
    DeployedJobConsumerFactory jcFactory = injector.getInstance(BaseWorkerModule.DeployedJobConsumerFactory.class);

    BaseWorker<?>[] hiddenWorkers = {
        injector.getInstance(PreRequestWorker.class),        
        injector.getInstance(PostRequestWorker.class)        
    };
    
    for(BaseWorker<?> worker:hiddenWorkers)    {
      jcFactory.create(worker, newJobAvailableTopic);
    }    
    BaseWorker<?>[] workers = {
        injector.getInstance(IngestTelephoneDummyWorker.class),
        injector.getInstance(IngestPeopleDummyWorker.class),
        injector.getInstance(IndexDatasetDummyWorker.class)
    };
    for(BaseWorker<?> worker:workers)    {
      OperationsSubscriberModule.deployOperationsSubscriberVerticle(injector, connection, worker);
      jcFactory.create(worker, newJobAvailableTopic);
    }
    connection.start();
  }

  static Injector createInjector(Connection connection, String dispatcherRpcAddr, String jobBoardRpcAddr) {
    return Guice.createInjector(
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Connection.class).toInstance(connection);
          }
        },
        new RpcClients4WorkerModule(connection, dispatcherRpcAddr, jobBoardRpcAddr),
        new OperationsSubscriberModule(),
        new BaseWorkerModule()
        );
  }
}
