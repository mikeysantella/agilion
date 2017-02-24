package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.OperationsApiService;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.Tasker_I;

public class MyOperationsApiService extends OperationsApiService {

  final RpcClientProvider<Tasker_I> tasker;
  final RpcClientProvider<OperationsRegistry_I> opRegistry;

  @Inject
  MyOperationsApiService(Supplier<Tasker_I> taskerF, Supplier<OperationsRegistry_I> opsRegF) {
    tasker=new RpcClientProvider<>(taskerF);
    opRegistry=new RpcClientProvider<>(opsRegF);
  }

  @Override
  public Response listOperations(SecurityContext securityContext)
      throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    // TODO: remove TEMPORARY: refresh every time
    if (true)
      refresh();

    return makeResultResponse("operationsList", opRegistry.rpc().listOperations());
  }

  private void refresh() {
    try {
      opRegistry.rpc().refresh().get();
      tasker.rpc().refreshJobsCreators().get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
