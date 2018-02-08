package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.JobApiService;
import dataengine.api.NotFoundException;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyJobApiService extends JobApiService {

  final RpcClientProvider<SessionsDB_I> sessDb;

  @Override
  public Response getJob(String id, SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: getJob: {}", securityContext.getUserPrincipal(), id);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    return makeResultResponse("Job", "job/", id, sessDb.rpc().getJob(id));
  }
}
