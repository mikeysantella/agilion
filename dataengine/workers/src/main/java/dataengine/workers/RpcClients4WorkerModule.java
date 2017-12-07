package dataengine.workers;

import javax.jms.Connection;
import com.google.inject.Provides;
import dataengine.apis.DepJobService_I;
import dataengine.apis.JobBoardOutput_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.CommunicationConsts;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.RpcClientsModule;

/// provides verticle clients used by Worker service
@Slf4j
class RpcClients4WorkerModule extends RpcClientsModule {

  public RpcClients4WorkerModule(Connection connection) {
    super(connection);
//    debug = true;
    log.debug("RpcClients4WorkerModule configured");
  }

  @Provides
  RpcClientProvider<Tasker_I> tasker_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(Tasker_I.class, CommunicationConsts.TASKER_RPCADDR));
  }

  @Provides
  RpcClientProvider<DepJobService_I> depJobService_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(DepJobService_I.class, CommunicationConsts.depJobMgrBroadcastAddr));
  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(SessionsDB_I.class, CommunicationConsts.SESSIONDB_RPCADDR));
  }

  @Provides
  RpcClientProvider<JobBoardOutput_I> jobConsumer_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(JobBoardOutput_I.class, CommunicationConsts.jobBoardBroadcastAddr));
  }

}
