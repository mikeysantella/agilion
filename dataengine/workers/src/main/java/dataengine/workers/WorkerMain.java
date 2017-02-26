package dataengine.workers;

import java.util.concurrent.CompletableFuture;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
public class WorkerMain {

  public static void main(String[] args) {
    main(new CompletableFuture<>());
  }

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
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    ClusteredVertxConfig vertxConfig = new ClusteredVertxConfig();
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF, vertxConfig),
        new VertxRpcClients4WorkerModule(vertxF),
        new OperationsSubscriberModule(vertxF));
  }
}
