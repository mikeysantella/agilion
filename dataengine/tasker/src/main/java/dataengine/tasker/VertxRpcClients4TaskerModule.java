package dataengine.tasker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Singleton;

import com.google.inject.Provides;

import dataengine.apis.SessionsDB_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import net.deelam.vertx.jobboard.DepJobService_I;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

/// provides verticle clients used by Tasker service
class VertxRpcClients4TaskerModule extends VertxRpcClientsModule {

  public VertxRpcClients4TaskerModule(CompletableFuture<Vertx> vertxF) {
    super(vertxF);
    debug = true;
  }

  @Provides @Singleton
  Supplier<SessionsDB_I> getSessionsDBClient() {
    return getClientSupplierFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr); // blocks
  }
  
  @Provides @Singleton
  Supplier<DepJobService_I> getDepJobServiceClient() {
    return getClientSupplierFor(DepJobService_I.class, VerticleConsts.depJobMgrBroadcastAddr); // blocks
  }
}
