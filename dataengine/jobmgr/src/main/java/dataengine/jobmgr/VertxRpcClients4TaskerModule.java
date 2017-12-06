package dataengine.jobmgr;

import java.util.concurrent.CompletableFuture;
import javax.jms.Connection;
import com.google.inject.Provides;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.JobBoardInput_I;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

/// provides verticle clients used by JobMgr service
@Slf4j
class VertxRpcClients4JobMgrModule extends VertxRpcClientsModule {

  public VertxRpcClients4JobMgrModule(CompletableFuture<Vertx> vertxF, Connection connection) {
    super(vertxF, connection);
    //debug = true;
    log.debug("VertxRpcClients4JobMgrModule configured");
  }

  @Provides
  RpcClientProvider<JobBoardInput_I> jobProducer_RpcClient(){
    return new RpcClientProvider<>(getAmqClientSupplierFor(JobBoardInput_I.class, VerticleConsts.jobBoardBroadcastAddr));
  }

}
