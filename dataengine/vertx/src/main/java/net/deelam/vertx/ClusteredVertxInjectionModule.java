package net.deelam.vertx;

import java.util.concurrent.CompletableFuture;

import com.google.inject.AbstractModule;

import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ClusteredVertxInjectionModule extends AbstractModule {
  final CompletableFuture<Vertx> vertxF;
  final ClusteredVertxConfig vertxConfig;// TODO: 2: set cluster IPs from file

  @Override
  protected void configure() {
    // create clustered VertX
    if(!vertxF.isDone()){
      new Thread(() -> StartVertx.createClustered(vertxConfig, vertx -> {
        //System.out.println("=========  Vert.x service registered");
        vertxF.complete(vertx);
        log.info("VertX created");
      })).start();
    }
    
    if(!vertxF.isDone())
      log.info("Waiting for Vertx ... {}", vertxF);
    bind(Vertx.class).toInstance(vertxF.join());
    log.info("Got Vertx");
  }

}
