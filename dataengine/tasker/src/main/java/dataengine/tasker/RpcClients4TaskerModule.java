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

  public RpcClients4TaskerModule(Connection connection) {
    super(connection);
    //debug = true;
    log.debug("VertxRpcClients4TaskerModule configured");
  }

  @Provides
  RpcClientProvider<DepJobService_I> jobDispatcher_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(DepJobService_I.class, CommunicationConsts.depJobMgrBroadcastAddr));
  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(SessionsDB_I.class, CommunicationConsts.sessionDbBroadcastAddr));
  }
}
