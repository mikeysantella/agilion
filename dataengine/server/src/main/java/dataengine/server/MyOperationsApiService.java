package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.Operation;
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

  boolean autoRefreshOperations = true; // TODO: 6: load autoRefreshOperations from property file

  @Override
  public Response listOperations(SecurityContext securityContext)
      throws NotFoundException {
    log.info("REST {}: listOperations", securityContext.getUserPrincipal());
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    CompletableFuture<Collection<Operation>> listOpsF;
    if (autoRefreshOperations)
      listOpsF = refreshOperations().thenCompose((any) -> opRegistry.rpc().listOperations());
    else 
      listOpsF = opRegistry.rpc().listOperations();

    return makeResultResponse("operationsList", listOpsF);
  }

  static long minSecondsBetweenRefresh = 30; // TODO: 6: load minSecondsBetweenRefresh from property file
  long lastRefreshRequest = 0;

  @Override
  public Response refreshOperations(SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: refresh", securityContext.getUserPrincipal());
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    
    return makeResultResponse("refreshOperations", refreshOperations());
  }

  private CompletableFuture<Boolean> refreshOperations() {
    long now = System.currentTimeMillis();
    if (now - lastRefreshRequest > minSecondsBetweenRefresh) {
      lastRefreshRequest = now;
      log.info("  Refreshing operationsRegistry and jobCreators");
      return  opRegistry.rpc().refresh()
          .thenCompose((none) -> tasker.rpc().refreshJobsCreators())
          .thenAccept((a) -> lastRefreshRequest = System.currentTimeMillis())
          .thenApply((a) -> true);
    } else
      return CompletableFuture.completedFuture(false);
  }

}
