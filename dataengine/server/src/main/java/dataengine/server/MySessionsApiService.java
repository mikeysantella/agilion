package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.NotFoundException;
import dataengine.api.SessionsApiService;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MySessionsApiService extends SessionsApiService {

  final RpcClientProvider<SessionsDB_I> sessDb;
  
  @Override
  public Response listSessions(SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: listSessions", securityContext.getUserPrincipal());
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionList", sessDb.rpc().listSessions());
  }

  @Override
  public Response listSessionIds(SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: listSessionIds", securityContext.getUserPrincipal());
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionIdList", sessDb.rpc().listSessionIds());
  }

  @Override
  public Response listSessionNames(SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: listSessionNames", securityContext.getUserPrincipal());
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;
    return makeResultResponse("sessionNameList", sessDb.rpc().listSessionNames());
  }

}
