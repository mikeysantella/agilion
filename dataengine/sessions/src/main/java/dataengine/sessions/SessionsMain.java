package dataengine.sessions;

import java.util.concurrent.CompletableFuture;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
public class SessionsMain {
  
  public static void main(String[] args) {
    main(new CompletableFuture<>());
  }
  public static void main(CompletableFuture<Vertx> vertxF) {
    System.out.println("Starting ... ");
    log.info("Starting {}", SessionsMain.class);
    Injector injector = createInjector(vertxF);
    TinkerGraphSessionsDbModule.deploySessionDb(injector);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    ClusteredVertxConfig vertxConfig=new ClusteredVertxConfig();
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF, vertxConfig),
        new TinkerGraphSessionsDbModule());
  }
}
