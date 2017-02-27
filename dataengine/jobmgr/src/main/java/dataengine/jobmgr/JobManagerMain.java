package dataengine.jobmgr;

import java.util.concurrent.CompletableFuture;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
public class JobManagerMain {

  public static void main(String[] args) {
    main(new CompletableFuture<>());
  }
  public static void main(CompletableFuture<Vertx> vertxF) {
    log.info("Starting {}", JobManagerMain.class);
    Injector injector = createInjector(vertxF);
    JobBoardModule.deployJobBoardVerticles(injector);
    JobBoardModule.deployDepJobService(injector, VerticleConsts.depJobMgrBroadcastAddr);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF),
        new JobBoardModule(VerticleConsts.jobBoardBroadcastAddr)
        );
  }
}
