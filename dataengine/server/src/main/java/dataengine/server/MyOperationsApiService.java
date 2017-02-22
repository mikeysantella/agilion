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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyOperationsApiService extends OperationsApiService {
  
  final Supplier<OperationsRegistry_I> opRegF;
  
  @Getter(lazy = true)
  private final OperationsRegistry_I opRegistry = lazyCreateOpsRegistryClient();

  OperationsRegistry_I lazyCreateOpsRegistryClient() {
    log.info("-- initializing instance "+this);
    return opRegF.get();
  }
  
  @Override
  public Response listOperations(SecurityContext securityContext)
      throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    
    if(true)
      try {
        getOpRegistry().refresh().get();   // TODO: remove TEMPORARY: refresh every time
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }     

   return makeResultResponse("operationsList", getOpRegistry().listOperations());
  }
}
