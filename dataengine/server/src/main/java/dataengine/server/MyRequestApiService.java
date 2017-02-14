package dataengine.server;

import static dataengine.server.RestParameterHelper.makeBadRequestResponse;
import static dataengine.server.RestParameterHelper.makeResponseIfIdInvalid;
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
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyRequestApiService extends RequestApiService {

  private static final String OBJECT_TYPE = "Request";

  final Tasker_I tasker;
  final SessionsDB_I sessionsDB;

  @Override
  public Response submitRequest(Request req, SecurityContext securityContext)
      throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    if (req!=null && req.getSessionId() == null)
      return makeBadRequestResponse("Submitted Request must have a sessionId! " + req);

    return tryCreateObject(OBJECT_TYPE, req, "request/", (r)->r.getId(),
        sessionsDB::hasRequest,
        () -> tasker.submitRequest(req));
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

    return sessionsDB.getRequest(id);
  }

}
