package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.OperationsApiService;
import dataengine.apis.OperationsRegistry_I;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyOperationsApiService extends OperationsApiService {
  final OperationsRegistry_I opReg;

  private static final String OBJECT_TYPE = "Operation";
  
  @Override
  public Response listOperations(SecurityContext securityContext)
      throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

   return makeResultResponse("operationsList", opReg.listOperations());
  }
}
