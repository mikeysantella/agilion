package dataengine.tasker;

import java.util.concurrent.CompletableFuture;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
public class TaskerMain {

  public static void main(String[] args) {
    main(new CompletableFuture<>());
  }

  public static void main(CompletableFuture<Vertx> vertxF) {
    log.info("Starting {}", TaskerMain.class);
    Injector injector = createInjector(vertxF);
    
    OperationsRegistryModule.deployOperationsRegistry(injector);
    TaskerModule.deployTasker(injector);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF),
        new VertxRpcClients4TaskerModule(vertxF),
        new OperationsRegistryModule(),
        new TaskerModule()
        );
  }

}
