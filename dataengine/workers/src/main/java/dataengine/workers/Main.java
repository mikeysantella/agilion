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
public class Main {
  
  public static void main(String[] args) {
    log.info("Starting {}", Main.class);
    Injector injector = createInjector(new CompletableFuture<>());
//    Vertx vertx = injector.getInstance(Vertx.class);
    
//    Tasker_I taskerSvc = injector.getInstance(Tasker_I.class);
//    new RpcVerticleServer(vertx, VerticleConsts.taskerBroadcastAddr)
//        .start("TaskerServiceBusAddr", taskerSvc);

    OperationsSubscriberVerticle opsRegVert= injector.getInstance(OperationsSubscriberVerticle.class);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    ClusteredVertxConfig vertxConfig = new ClusteredVertxConfig();
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF, vertxConfig),
        //new VertxRpcClients4TaskerModule(vertxF),
        new AbstractModule() {
          Vertx vertx;
          @Override
          protected void configure() {
            try {
              vertx=vertxF.get();
            } catch (InterruptedException | ExecutionException e) {
              e.printStackTrace();
            }
            
//            bind(OperationsRegistry_I.class).to(OperationsRegistryService.class).asEagerSingleton();
//            bind(Tasker_I.class).to(TaskerService.class).asEagerSingleton();
          }
          
          int subscriberCounter=0;
          @Provides
          OperationsSubscriberVerticle createOperationsSubscriberVerticle(){
            OperationsSubscriberVerticle opsRegVert=  new OperationsSubscriberVerticle(VerticleConsts.opsRegBroadcastAddr, 
                "operations"+(++subscriberCounter)+System.currentTimeMillis());
            vertx.deployVerticle(opsRegVert);
            return opsRegVert;
          }
        });
  }
}
