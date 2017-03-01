package dataengine.workers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxInjectionModule;
import net.deelam.vertx.jobboard.JobConsumer;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class WorkerMain {

  public static void main(String[] args) {
    main(new CompletableFuture<>());
  }

  final List<JobConsumer> jobConsumers;
  
  public static void main(CompletableFuture<Vertx> vertxF) {
    log.info("Starting {}", WorkerMain.class);
    Injector injector = createInjector(vertxF);

    {
      Worker_I worker = new IngestTelephoneWorker();
      OperationsSubscriberModule.deployOperationsSubscriberVerticle(injector, worker);
    }

    {
      Worker_I worker = new IngestPeopleWorker();
      OperationsSubscriberModule.deployOperationsSubscriberVerticle(injector, worker);
    }

    {
      Worker_I worker = new IndexDatasetWorker();
      OperationsSubscriberModule.deployOperationsSubscriberVerticle(injector, worker);
    }
    
    WorkerMain workerMain = injector.getInstance(WorkerMain.class);
    log.info("jobConsumers={}", workerMain.jobConsumers);
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
