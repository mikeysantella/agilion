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
public class RpcClients4WorkerModule extends RpcClientsModule {
  private final String depJobMgrBroadcastAddr;
  private final String jobBoardBroadcastAddr;
  final int deliveryMode;

  public RpcClients4WorkerModule(Connection connection, String depJobMgrBroadcastAddr, String jobBoardBroadcastAddr, int deliveryMode) {
    super(connection);
    this.depJobMgrBroadcastAddr=depJobMgrBroadcastAddr;
    this.jobBoardBroadcastAddr=jobBoardBroadcastAddr;
//    debug = true;
    this.deliveryMode=deliveryMode;
    log.debug("RpcClients4WorkerModule configured");
  }

  @Provides
  RpcClientProvider<Tasker_I> taskerRpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(Tasker_I.class, CommunicationConsts.TASKER_RPCADDR, deliveryMode));
  }

  @Provides
  RpcClientProvider<DepJobService_I> depJobServiceRpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(DepJobService_I.class, depJobMgrBroadcastAddr, deliveryMode));
  }

  @Provides
  RpcClientProvider<SessionsDB_I> sessionsDbRpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(SessionsDB_I.class, CommunicationConsts.SESSIONDB_RPCADDR, deliveryMode));
  }

  @Provides
  RpcClientProvider<JobBoardOutput_I> jobConsumerRpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(JobBoardOutput_I.class, jobBoardBroadcastAddr, deliveryMode));
  }

}
