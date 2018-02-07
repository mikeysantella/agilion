package dataengine.server;

import javax.jms.Connection;
import com.google.inject.Provides;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.RpcClientsModule;

/// provides verticle clients used by REST services

@Slf4j
class RpcClients4ServerModule extends RpcClientsModule {

  final int deliveryMode;
  
  public RpcClients4ServerModule(Connection connection, int deliveryMode) {
    super(connection);
    //debug=true;
    this.deliveryMode=deliveryMode;
    log.debug("RpcClients4ServerModule configured");
  }
  
  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(SessionsDB_I.class, CommunicationConsts.SESSIONDB_RPCADDR, deliveryMode));
  }

  @Provides
  RpcClientProvider<OperationsRegistry_I> opsReg_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(OperationsRegistry_I.class, CommunicationConsts.OPSREGISTRY_RPCADDR, deliveryMode));
  }

  @Provides
  RpcClientProvider<Tasker_I> tasker_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(Tasker_I.class, CommunicationConsts.TASKER_RPCADDR, deliveryMode));
  }

}
