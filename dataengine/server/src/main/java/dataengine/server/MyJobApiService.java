package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.JobApiService;
import dataengine.api.NotFoundException;
import dataengine.apis.SessionsDB_I;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyJobApiService extends JobApiService {
  
  final Supplier<SessionsDB_I> sessionsDBF;
  
  @Getter(lazy = true)
  private final SessionsDB_I sessionsDB = lazyCreateSessionsDbClient();

  SessionsDB_I lazyCreateSessionsDbClient() {
    log.info("-- initializing instance "+this);
    return sessionsDBF.get();
  }
  
  @Override
  public Response getJob(String id, SecurityContext securityContext) throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    return makeResultResponse("Job", "job/", id, getSessionsDB().getJob(id));
  }
}
