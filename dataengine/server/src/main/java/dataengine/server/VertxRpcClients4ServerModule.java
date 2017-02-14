package dataengine.server;

import java.util.concurrent.CompletableFuture;

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

  SessionsDB_I sessDbClient=null;
  @Provides
  SessionsDB_I getSessionsDBClient() {
    if(sessDbClient==null)
      sessDbClient=getClientFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr); // blocks
    return sessDbClient;
  }

  Tasker_I taskerClient=null;
  @Provides
  Tasker_I getTaskerClient() {
    if(taskerClient==null)
      taskerClient=getClientFor(Tasker_I.class, VerticleConsts.taskerBroadcastAddr); // blocks
    return taskerClient;
  }

  OperationsRegistry_I opsRegClient=null;
  @Provides
  OperationsRegistry_I getOperationsRegistryClient() {
    if(opsRegClient==null)
      opsRegClient=getClientFor(OperationsRegistry_I.class, VerticleConsts.opsRegBroadcastAddr); // blocks
    return opsRegClient;
  }
}
