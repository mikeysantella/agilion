package dataengine.server;

import static dataengine.server.RestParameterHelper.makeResponseIfIdInvalid;
import static dataengine.server.RestParameterHelper.makeResponseIfNotSecure;
import static dataengine.server.RestParameterHelper.makeResultResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.DatasetApiService;
import dataengine.api.NotFoundException;
import dataengine.apis.SessionsDB_I;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MyDatasetApiService extends DatasetApiService {
  private static final String OBJECT_TYPE = "Dataset";
  final SessionsDB_I sessionsDB;
    @Override
    public Response getDataset(String id, SecurityContext securityContext)
    throws NotFoundException {
      Response resp = makeResponseIfNotSecure(securityContext);
      if (resp != null)
        return resp;

      resp = makeResponseIfIdInvalid(OBJECT_TYPE, id);
      if (resp != null)
        return resp;
      return makeResultResponse(OBJECT_TYPE, "dataset/", id, sessionsDB.getDataset(id));
    }
}
