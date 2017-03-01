package dataengine.server;

import java.util.concurrent.CompletableFuture;

import com.google.inject.Provides;

import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.DepJobService_I;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

/// provides verticle clients used by REST services

@Slf4j
class VertxRpcClients4ServerModule extends VertxRpcClientsModule {
  
  public VertxRpcClients4ServerModule(CompletableFuture<Vertx> vertxF) {
    super(vertxF);
    //debug=true;
    log.debug("VertxRpcClients4ServerModule configured");
  }
  
  @Provides
  RpcClientProvider<DepJobService_I> jobDispatcher_RpcClient(){
    return new RpcClientProvider<>(getClientSupplierFor(DepJobService_I.class, VerticleConsts.depJobMgrBroadcastAddr));
  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient(){
    return new RpcClientProvider<>(getClientSupplierFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr));
  }

  @Provides
  RpcClientProvider<OperationsRegistry_I> opsReg_RpcClient(){
    return new RpcClientProvider<>(getClientSupplierFor(OperationsRegistry_I.class, VerticleConsts.opsRegBroadcastAddr));
  }

  @Provides
  RpcClientProvider<Tasker_I> tasker_RpcClient(){
    return new RpcClientProvider<>(getClientSupplierFor(Tasker_I.class, VerticleConsts.taskerBroadcastAddr));
  }

}
