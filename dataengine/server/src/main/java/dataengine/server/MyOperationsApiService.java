package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.OperationsApiService;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.Tasker_I;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyOperationsApiService extends OperationsApiService {

  final RpcClientProvider<Tasker_I> tasker;
  final RpcClientProvider<OperationsRegistry_I> opRegistry;

  @Inject
  MyOperationsApiService(Supplier<Tasker_I> taskerF, Supplier<OperationsRegistry_I> opsRegF) {
    tasker = new RpcClientProvider<>(taskerF);
    opRegistry = new RpcClientProvider<>(opsRegF);
  }

  static long minTimeBetweenRefresh=30_000; // 30 seconds  TODO: 6: load from property file
  long lastRefreshRequest=0;
  
  @Override
  public Response listOperations(SecurityContext securityContext)
      throws NotFoundException {
    log.info("REST {}: listOperations", securityContext.getUserPrincipal());
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    long now = System.currentTimeMillis();
    if (now-lastRefreshRequest > minTimeBetweenRefresh){
      lastRefreshRequest=now;
      refresh().join();
      lastRefreshRequest=System.currentTimeMillis();
    }
    return makeResultResponse("operationsList", opRegistry.rpc().listOperations());
  }

  private CompletableFuture<Void> refresh() {
    log.info("  Refreshing operationsRegistry and jobCreators");
    return opRegistry.rpc().refresh().thenAccept((f) -> tasker.rpc().refreshJobsCreators());
  }
}
