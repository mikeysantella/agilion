package dataengine.server;

import static dataengine.server.RestParameterHelper.*;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.SessionsApiService;
import dataengine.apis.SessionsDB_I;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MySessionsApiService extends SessionsApiService {

  final Supplier<SessionsDB_I> sessionsDBF;
  
  @Getter(lazy = true)
  private final SessionsDB_I sessionsDb = lazyCreateSessionsDbClient();

  SessionsDB_I lazyCreateSessionsDbClient() {
    log.info("-- initializing instance "+this);
    return sessionsDBF.get();
  }
  
  @Override
  public Response listSessions(SecurityContext securityContext) throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionList", getSessionsDb().listSessions());
  }

  @Override
  public Response listSessionIds(SecurityContext securityContext) throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionIdList", getSessionsDb().listSessionIds());
  }

  @Override
  public Response listSessionNames(SecurityContext securityContext) throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionNameList", getSessionsDb().listSessionNames());
  }

}
