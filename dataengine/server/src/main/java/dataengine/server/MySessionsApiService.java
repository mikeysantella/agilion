package dataengine.server;

import static dataengine.server.RestParameterHelper.*;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.SessionsApiService;
import dataengine.apis.SessionsDB_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MySessionsApiService extends SessionsApiService {
  final SessionsDB_I sessionsDB;

  @Override
  public Response listSessions(SecurityContext securityContext) throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionList", sessionsDB.listSessions());
  }

  @Override
  public Response listSessionIds(SecurityContext securityContext) throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionIdList", sessionsDB.listSessionIds());
  }

  @Override
  public Response listSessionNames(SecurityContext securityContext) throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionNameList", sessionsDB.listSessionNames());
  }

}
