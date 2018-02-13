package dataengine.server;

import static dataengine.server.RestParameterHelper.makeBadResponseIfIdInvalid;
import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.DatasetApiService;
import dataengine.api.NotFoundException;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyDatasetApiService extends DatasetApiService {
  private static final String OBJECT_TYPE = "Dataset";

  final RpcClientProvider<SessionsDB_I> sessDb;

  @Override
  public Response getDataset(String id, SecurityContext securityContext)
      throws NotFoundException {
    log.info("REST {}: getDataset: {}", securityContext.getUserPrincipal(), id);
    Response resp = makeResponseIfNotSecure(securityContext);
    if (resp != null)
      return resp;

    resp = makeBadResponseIfIdInvalid(OBJECT_TYPE, id);
    if (resp != null)
      return resp;
    return makeResultResponse(OBJECT_TYPE, "dataset/", id, sessDb.rpc().getDataset(id));
  }

}
