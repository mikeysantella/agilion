package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.JobApiService;
import dataengine.api.NotFoundException;
import dataengine.apis.SessionsDB_I;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyJobApiService extends JobApiService {
  final SessionsDB_I sessionsDB;
  
  @Override
  public Response getJob(String id, SecurityContext securityContext) throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    return makeResultResponse("Job", "job/", id, sessionsDB.getJob(id));
  }
}
