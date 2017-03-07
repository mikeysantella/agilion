package dataengine.workers;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.apis.VerticleConsts;
import dataengine.workers.BaseWorkerModule.DeployedJobConsumerFactory;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class WorkerMain {

  public static void main(String[] args) throws IOException {
    main(new CompletableFuture<>());
  }
  
  public static void main(CompletableFuture<Vertx> vertxF) throws IOException {
    log.info("Starting {}", WorkerMain.class);
    Properties properties=new Properties();
    PropertiesUtil.loadProperties("workers.props", properties);
    Injector injector = createInjector(vertxF);
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
      OperationsSubscriberModule.deployOperationsSubscriberVerticle(injector, worker);
      jcFactory.create(worker);
    }    
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF),
        new VertxRpcClients4WorkerModule(vertxF),
        new OperationsSubscriberModule(),
        new BaseWorkerModule(VerticleConsts.jobBoardBroadcastAddr)
        );
  }
}
