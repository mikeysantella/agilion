package dataengine.tasker;

import java.util.concurrent.CompletableFuture;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.ClusteredVertxInjectionModule;
import net.deelam.vertx.RegistryVerticle;
import net.deelam.vertx.rpc.RpcVerticleServer;

@Slf4j
public class Main {
  
  public static void main(String[] args) {
    log.info("Starting {}", Main.class);
    Injector injector = createInjector(new CompletableFuture<>());
    Vertx vertx = injector.getInstance(Vertx.class);
    
    OperationsRegistry_I opsRegSvc = injector.getInstance(OperationsRegistry_I.class);
    new RpcVerticleServer(vertx, VerticleConsts.opsRegBroadcastAddr)
        .start("OperationsRegServiceBusAddr", opsRegSvc);
    
    Tasker_I taskerSvc = injector.getInstance(Tasker_I.class);
    new RpcVerticleServer(vertx, VerticleConsts.taskerBroadcastAddr)
        .start("TaskerServiceBusAddr", taskerSvc);

    OperationsRegistryVerticle opsRegVert= injector.getInstance(OperationsRegistryVerticle.class);
    vertx.deployVerticle(opsRegVert);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    ClusteredVertxConfig vertxConfig = new ClusteredVertxConfig();
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF, vertxConfig),
        new VertxRpcClients4TaskerModule(vertxF),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(OperationsRegistry_I.class).to(OperationsRegistryService.class).asEagerSingleton();
            bind(Tasker_I.class).to(TaskerService.class).asEagerSingleton();
            
            OperationsRegistryVerticle opsRegVert = new OperationsRegistryVerticle(VerticleConsts.opsRegBroadcastAddr);
            bind(OperationsRegistryVerticle.class).toInstance(opsRegVert);
          }
        });
  }
}
