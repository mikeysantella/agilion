package dataengine.server;

import static dataengine.server.RestParameterHelper.makeBadRequestResponse;
import static dataengine.server.RestParameterHelper.makeBadResponseIfIdInvalid;
import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;
import static dataengine.server.RestParameterHelper.tryCreateObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.Request;
import dataengine.api.RequestApiService;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyRequestApiService extends RequestApiService {

  final RpcClientProvider<Tasker_I> tasker;
  final RpcClientProvider<SessionsDB_I> sessDb;

  private static final String OBJECT_TYPE = "Request";

  /// 

  @Override
  public Response submitRequest(Request req, SecurityContext securityContext)
      throws NotFoundException {
    log.info("REST {}: submitRequest: {}", securityContext.getUserPrincipal(), req);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    if (req != null && req.getSessionId() == null)
      return makeBadRequestResponse("Submitted Request must have a sessionId! " + req);

    return tryCreateObject(OBJECT_TYPE, req, "request/", (r) -> r.getId(),
        sessDb.rpc()::hasRequest,
        () -> tasker.rpc().submitRequest(req));
  }

  @Override
  public Response getRequest(String id, SecurityContext securityContext)
      throws NotFoundException {
    log.info("REST {}: getRequest: {}", securityContext.getUserPrincipal(), id);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    resp = makeBadResponseIfIdInvalid(OBJECT_TYPE, id);
    if (resp != null)
      return resp;

    return makeResultResponse(OBJECT_TYPE, "request/", id, getRequest(id));
  }

  private Future<Request> getRequest(String id) {
    if ("null".equalsIgnoreCase(id)) // for testing
      return CompletableFuture.completedFuture(new Request());

    return sessDb.rpc().getRequest(id);
  }

}
