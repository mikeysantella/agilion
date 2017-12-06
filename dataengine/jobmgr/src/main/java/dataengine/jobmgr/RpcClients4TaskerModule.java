package dataengine.jobmgr;

import javax.jms.Connection;
import com.google.inject.Provides;
import dataengine.apis.JobBoardInput_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.CommunicationConsts;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.RpcClientsModule;

/// provides verticle clients used by JobMgr service
@Slf4j
class RpcClients4JobMgrModule extends RpcClientsModule {

  public RpcClients4JobMgrModule(Connection connection) {
    super(connection);
    log.debug("VertxRpcClients4JobMgrModule configured");
  }

  @Provides
  RpcClientProvider<JobBoardInput_I> jobProducer_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(JobBoardInput_I.class, CommunicationConsts.jobBoardBroadcastAddr));
  }

}
