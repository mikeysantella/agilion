package dataengine.workers;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.apis.VerticleConsts;
import dataengine.workers.BaseWorkerModule.DeployedJobConsumerFactory;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class WorkerMain {

  public static void main(String[] args) throws Exception {
    main(new CompletableFuture<>(), null);
  }
  
  public static void main(CompletableFuture<Vertx> vertxF, String brokerUrl) throws IOException, JMSException {
    log.info("Starting {}", WorkerMain.class);
    Properties properties=new Properties();
    PropertiesUtil.loadProperties("workers.props", properties);
    Connection connection = MQClient.connect(brokerUrl);
    Injector injector = createInjector(vertxF, connection);
    DeployedJobConsumerFactory jcFactory = injector.getInstance(BaseWorkerModule.DeployedJobConsumerFactory.class);

    BaseWorker<?>[] hiddenWorkers = {
        injector.getInstance(PreRequestWorker.class),        
        injector.getInstance(PostRequestWorker.class)        
    };
    for(BaseWorker<?> worker:hiddenWorkers)    {
      jcFactory.create(worker);
    }    
    BaseWorker<?>[] workers = {
        injector.getInstance(IngestTelephoneDummyWorker.class),
        injector.getInstance(IngestPeopleDummyWorker.class),
        injector.getInstance(IndexDatasetDummyWorker.class)
    };
    for(BaseWorker<?> worker:workers)    {
      OperationsSubscriberModule.deployOperationsSubscriberVerticle(injector, connection, worker);
      jcFactory.create(worker);
    }
    connection.start();
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF, Connection connection) {
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF),
        new VertxRpcClients4WorkerModule(vertxF, connection),
        new OperationsSubscriberModule(),
        new BaseWorkerModule(VerticleConsts.jobBoardBroadcastAddr)
        );
  }
}
