package dataengine.server;

import static dataengine.server.RestParameterHelper.makeBadResponseIfIdInvalid;
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

  private static final String OBJECT_TYPE = "Job";

  @Override
  public Response getJob(String id, SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: getJob: {}", securityContext.getUserPrincipal(), id);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    resp = makeBadResponseIfIdInvalid(OBJECT_TYPE, id);
    if (resp != null)
      return resp;

    return makeResultResponse(OBJECT_TYPE, "job/", id, sessDb.rpc().getJob(id));
  }
}
