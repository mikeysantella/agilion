package dataengine.server;

import javax.jms.Connection;
import com.google.inject.Provides;
import dataengine.apis.DepJobService_I;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.CommunicationConsts;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.RpcClientsModule;

/// provides verticle clients used by REST services

@Slf4j
class RpcClients4ServerModule extends RpcClientsModule {
  
  public RpcClients4ServerModule(Connection connection) {
    super(connection);
    //debug=true;
    log.debug("RpcClients4ServerModule configured");
  }
  
  @Provides
  RpcClientProvider<DepJobService_I> jobDispatcher_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(DepJobService_I.class, CommunicationConsts.depJobMgrBroadcastAddr));
  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(SessionsDB_I.class, CommunicationConsts.SESSIONDB_RPCADDR));
  }

  @Provides
  RpcClientProvider<OperationsRegistry_I> opsReg_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(OperationsRegistry_I.class, CommunicationConsts.OPSREGISTRY_RPCADDR));
  }

  @Provides
  RpcClientProvider<Tasker_I> tasker_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(Tasker_I.class, CommunicationConsts.TASKER_RPCADDR));
  }

}
