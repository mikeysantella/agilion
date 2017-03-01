package dataengine.tasker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Singleton;

import com.google.inject.Provides;

import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.DepJobService_I;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

/// provides verticle clients used by Tasker service
@Slf4j
class VertxRpcClients4TaskerModule extends VertxRpcClientsModule {

  public VertxRpcClients4TaskerModule(CompletableFuture<Vertx> vertxF) {
    super(vertxF);
    //debug = true;
    log.debug("VertxRpcClients4TaskerModule configured");
  }
//
//  @Provides @Singleton
//  Supplier<SessionsDB_I> getSessionsDBClient() {
//    return getClientSupplierFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr); // blocks
//  }
//  
//  @Provides @Singleton
//  Supplier<DepJobService_I> getDepJobServiceClient() {
//    return getClientSupplierFor(DepJobService_I.class, VerticleConsts.depJobMgrBroadcastAddr); // blocks
//  }
  

  @Provides
  RpcClientProvider<DepJobService_I> jobDispatcher_RpcClient(){
    return new RpcClientProvider<>(getClientSupplierFor(DepJobService_I.class, VerticleConsts.depJobMgrBroadcastAddr));
  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient(){
    return new RpcClientProvider<>(getClientSupplierFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr));
  }
}
