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
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyRequestApiService extends RequestApiService {

  private static final String OBJECT_TYPE = "Request";

  final Supplier<Tasker_I> taskerF;
  
  @Getter(lazy = true)
  private final Tasker_I tasker = lazyCreateTaskerClient();

  Tasker_I lazyCreateTaskerClient() {
    log.info("-- initializing instance "+this);
    return taskerF.get();
  }

  //
  
  final Supplier<SessionsDB_I> sessionsDBF;
  
  @Getter(lazy = true)
  private final SessionsDB_I sessionsDb = lazyCreateSessionsDbClient();

  SessionsDB_I lazyCreateSessionsDbClient() {
    log.info("-- initializing instance "+this);
    return sessionsDBF.get();
  }
  
  /// 
  
  @Override
  public Response submitRequest(Request req, SecurityContext securityContext)
      throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    if (req!=null && req.getSessionId() == null)
      return makeBadRequestResponse("Submitted Request must have a sessionId! " + req);

    return tryCreateObject(OBJECT_TYPE, req, "request/", (r)->r.getId(),
        getSessionsDb()::hasRequest,
        () -> taskerF.get().submitRequest(req));
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

    return getSessionsDb().getRequest(id);
  }

}
