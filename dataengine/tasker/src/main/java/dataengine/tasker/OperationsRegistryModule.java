package dataengine.tasker;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.rpc.RpcVerticleServer;

@Slf4j
final class OperationsRegistryModule extends AbstractModule {
  @Override
  protected void configure() {
    requireBinding(Vertx.class);
    
    // See http://stackoverflow.com/questions/14781471/guice-differences-between-singleton-class-and-singleton

    // OperationsRegistry_I used by clients
    // OperationsRegistryRpcService uses OperationsRegistryVerticle
    bind(OperationsRegistry_I.class).to(OperationsRegistryRpcService.class);
    bind(OperationsRegistryRpcService.class).in(Singleton.class);

    // OperationsRegistryVerticle to which operations are registered by providers (ie, Workers)
    OperationsRegistryVerticle opsRegVert = new OperationsRegistryVerticle(VerticleConsts.opsRegBroadcastAddr);
    bind(OperationsRegistryVerticle.class).toInstance(opsRegVert);
  }

  static void deployOperationsRegistry(Injector injector) {
    Vertx vertx = injector.getInstance(Vertx.class);
  
    OperationsRegistryVerticle opsRegVert = injector.getInstance(OperationsRegistryVerticle.class);
    log.info("VERTX: TASKER: Deploying OperationsRegistryVerticle: {} ", opsRegVert); 
    vertx.deployVerticle(opsRegVert);
  
    OperationsRegistry_I opsRegSvc = injector.getInstance(OperationsRegistry_I.class);
    log.info("VERTX: TASKER: Deploying RPC service for OperationsRegistry_I: {} ", opsRegVert); 
    new RpcVerticleServer(vertx, VerticleConsts.opsRegBroadcastAddr)
        .start("OperationsRegServiceBusAddr", opsRegSvc);
  }
}