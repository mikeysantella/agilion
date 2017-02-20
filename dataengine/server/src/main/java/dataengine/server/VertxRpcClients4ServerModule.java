package dataengine.server;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import com.google.inject.Provides;

import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

/// provides verticle clients used by REST services

@Slf4j
class VertxRpcClients4ServerModule extends VertxRpcClientsModule {
  
  public VertxRpcClients4ServerModule(CompletableFuture<Vertx> vertxF) {
    super(vertxF);
    debug=true;
  }

  @Provides @Singleton
  SessionsDB_I getSessionsDBClient() {
    return getClientFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr); // blocks
  }

  @Provides @Singleton
  Tasker_I getTaskerClient() {
    return getClientFor(Tasker_I.class, VerticleConsts.taskerBroadcastAddr); // blocks
  }

  @Provides @Singleton
  OperationsRegistry_I getOperationsRegistryClient() {
    return getClientFor(OperationsRegistry_I.class, VerticleConsts.opsRegBroadcastAddr); // blocks
  }
}
