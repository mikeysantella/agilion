package dataengine.tasker;

import java.util.concurrent.CompletableFuture;

import com.google.inject.Provides;

import dataengine.apis.SessionsDB_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

/// provides verticle clients used by Tasker service
class VertxRpcClients4TaskerModule extends VertxRpcClientsModule {

  public VertxRpcClients4TaskerModule(CompletableFuture<Vertx> vertxF) {
    super(vertxF);
    debug = true;
  }
  
  SessionsDB_I sessDbClient=null;
  @Provides
  SessionsDB_I getSessionsDBClient() {
    if(sessDbClient==null)
      sessDbClient=getClientFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr); // blocks
    return sessDbClient;
  }
}
