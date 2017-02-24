package dataengine.server;

import static dataengine.server.RestParameterHelper.makeBadRequestResponse;
import static dataengine.server.RestParameterHelper.makeResponseIfIdInvalid;
import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;
import static dataengine.server.RestParameterHelper.tryCreateObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.Request;
import dataengine.api.RequestApiService;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;

public class MyRequestApiService extends RequestApiService {

  final RpcClientProvider<Tasker_I> tasker;
  final RpcClientProvider<SessionsDB_I> sessDb;

  @Inject
  MyRequestApiService(Supplier<Tasker_I> taskerF, Supplier<SessionsDB_I> sessionsDbF) {
    tasker=new RpcClientProvider<>(taskerF);
    sessDb=new RpcClientProvider<>(sessionsDbF);
  }

  private static final String OBJECT_TYPE = "Request";

  /// 

  @Override
  public Response submitRequest(Request req, SecurityContext securityContext)
      throws NotFoundException {
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
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    resp = makeResponseIfIdInvalid(OBJECT_TYPE, id);
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
