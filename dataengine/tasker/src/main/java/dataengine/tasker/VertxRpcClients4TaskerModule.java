package dataengine.tasker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

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

  @Provides @Singleton
  SessionsDB_I getSessionsDBClient() {
    return getClientFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr); // blocks
  }
}
