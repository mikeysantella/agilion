package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.JobApiService;
import dataengine.api.NotFoundException;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyJobApiService extends JobApiService {

  final RpcClientProvider<SessionsDB_I> sessDb;

  @Inject
  MyJobApiService(Supplier<SessionsDB_I> sessionsDbF) {
    sessDb=new RpcClientProvider<>(sessionsDbF);
  }

  @Override
  public Response getJob(String id, SecurityContext securityContext) throws NotFoundException {
    log.info("REST {}: getJob: {}", securityContext.getUserPrincipal(), id);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    return makeResultResponse("Job", "job/", id, sessDb.rpc().getJob(id));
  }
}
