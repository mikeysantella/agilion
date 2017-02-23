package dataengine.workers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
public class WorkerMain {

  public static void main(String[] args) {
    log.info("Starting {}", WorkerMain.class);
    Injector injector = createInjector(new CompletableFuture<>());
    OperationsSubscriberVerticle opsRegVert = injector.getInstance(OperationsSubscriberVerticle.class);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    ClusteredVertxConfig vertxConfig = new ClusteredVertxConfig();
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF, vertxConfig),
        //TODO: new VertxRpcClients4WorkerModule(vertxF),
        new AbstractModule() {
          Vertx vertx;

          @Override
          protected void configure() {
            try {
              vertx = vertxF.get();
            } catch (InterruptedException | ExecutionException e) {
              e.printStackTrace();
            }
          }

          int subscriberCounter = 0;

          @Provides
          OperationsSubscriberVerticle deployOperationsSubscriberVerticle() {
            OperationsSubscriberVerticle opsRegVert =
                new OperationsSubscriberVerticle(VerticleConsts.opsRegBroadcastAddr,
                    "operations" + (++subscriberCounter) + System.currentTimeMillis());
            vertx.deployVerticle(opsRegVert);
            return opsRegVert;
          }
        });
  }
}
