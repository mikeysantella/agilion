package dataengine.workers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Singleton;

import com.google.inject.Provides;

import dataengine.apis.JobListener_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.DepJobService_I;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

/// provides verticle clients used by Worker service
@Slf4j
class VertxRpcClients4WorkerModule extends VertxRpcClientsModule {

  public VertxRpcClients4WorkerModule(CompletableFuture<Vertx> vertxF) {
    super(vertxF);
    debug = true;
    log.debug("VertxRpcClients4WorkerModule configured");
  }

  @Provides @Singleton
  Supplier<JobListener_I> getJobListenerClient() {
    return getClientSupplierFor(JobListener_I.class, VerticleConsts.jobListenerBroadcastAddr); // blocks
  }
  
  @Provides @Singleton
  Supplier<DepJobService_I> getDepJobServiceClient() {
    return getClientSupplierFor(DepJobService_I.class, VerticleConsts.depJobMgrBroadcastAddr); // blocks
  }

}
