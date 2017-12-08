package dataengine.tasker;

import javax.jms.Connection;
import com.google.inject.Provides;
import dataengine.apis.DepJobService_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.CommunicationConsts;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.RpcClientsModule;

/// provides verticle clients used by Tasker service
@Slf4j
class RpcClients4TaskerModule extends RpcClientsModule {

//  private final String depJobMgrBroadcastAddr;

  public RpcClients4TaskerModule(Connection connection/*, String depJobMgrBroadcastAddr*/) {
    super(connection);
//    this.depJobMgrBroadcastAddr=depJobMgrBroadcastAddr;
    //debug = true;
    log.debug("VertxRpcClients4TaskerModule configured");
  }

//  @Provides
//  RpcClientProvider<DepJobService_I> jobDispatcherRpcClient(){
//    return new RpcClientProvider<>(getAmqClientSupplierFor(DepJobService_I.class, depJobMgrBroadcastAddr));
//  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDbRpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(SessionsDB_I.class, CommunicationConsts.SESSIONDB_RPCADDR));
  }
}
