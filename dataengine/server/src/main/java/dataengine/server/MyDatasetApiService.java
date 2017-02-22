package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfIdInvalid;
import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.DatasetApiService;
import dataengine.api.NotFoundException;
import dataengine.apis.SessionsDB_I;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyDatasetApiService extends DatasetApiService {
  private static final String OBJECT_TYPE = "Dataset";
  
  final Supplier<SessionsDB_I> sessionsDBF;
  
  @Getter(lazy = true)
  private final SessionsDB_I sessionsDB = lazyCreateSessionsDbClient();

  SessionsDB_I lazyCreateSessionsDbClient() {
    log.info("-- initializing instance "+this);
    return sessionsDBF.get();
  }

  @Override
  public Response getDataset(String id, SecurityContext securityContext)
      throws NotFoundException {
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    resp = makeResponseIfIdInvalid(OBJECT_TYPE, id);
    if (resp != null)
      return resp;
    return makeResultResponse(OBJECT_TYPE, "dataset/", id, getSessionsDB().getDataset(id));
  }
}
