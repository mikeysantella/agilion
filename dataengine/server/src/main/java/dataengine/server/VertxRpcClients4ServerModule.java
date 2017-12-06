package dataengine.server;

import javax.jms.Connection;
import com.google.inject.Provides;
import dataengine.apis.DepJobService_I;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.VertxRpcClientsModule;

/// provides verticle clients used by REST services

@Slf4j
class VertxRpcClients4ServerModule extends VertxRpcClientsModule {
  
  public VertxRpcClients4ServerModule(Connection connection) {
    super(connection);
    //debug=true;
    log.debug("VertxRpcClients4ServerModule configured");
  }
  
  @Provides
  RpcClientProvider<DepJobService_I> jobDispatcher_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(DepJobService_I.class, VerticleConsts.depJobMgrBroadcastAddr));
  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr));
  }

  @Provides
  RpcClientProvider<OperationsRegistry_I> opsReg_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(OperationsRegistry_I.class, VerticleConsts.opsRegBroadcastAddr));
  }

  @Provides
  RpcClientProvider<Tasker_I> tasker_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(Tasker_I.class, VerticleConsts.taskerBroadcastAddr));
  }

}
