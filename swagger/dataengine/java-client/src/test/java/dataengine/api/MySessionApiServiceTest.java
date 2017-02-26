package dataengine.api;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import javax.ws.rs.ProcessingException;

import org.junit.Ignore;
import org.junit.Test;

import dataengine.ApiClient;
import dataengine.ApiException;

@Ignore
public class MySessionApiServiceTest {
  
  public static void main(String[] args) throws ApiException {
    String baseUri = "http://localhost:8080/server/";
    if(args.length>0)
      baseUri=args[0];
    if(!baseUri.startsWith("http")){
      baseUri = "http://"+baseUri+":8080/server/";
    }
    if(!baseUri.contains("DataEngine"))
      baseUri += "deelam/DataEngine/0.0.1";
    System.out.println("Using baseUri="+baseUri);
    MySessionApiServiceTest me = new MySessionApiServiceTest(baseUri);
    me.testSessionsApi();
    me.testRequestsApi();
  }

  final SessionsApi sessApi;
  final RequestsApi reqApi;
  final DatasetsApi datasetApi;
  
  public MySessionApiServiceTest(String basePathUri) {
    ApiClient apiClient = new ApiClient().setBasePath(basePathUri);
    sessApi = new SessionsApi(apiClient);
    reqApi = new RequestsApi(apiClient);
    datasetApi = new DatasetsApi(apiClient);
  }

  @Test
  public void testSessionsApi() throws ApiException {
    String sessId = "newSess"+System.currentTimeMillis();
    Session session = new Session().id(sessId);
    Session session1 = sessApi.createSession(session);
      
    Session session2 = sessApi.getSession(sessId);
    assertEquals(session1, session2);
    session2.setLabel(null); // ignore
    session2.setCreatedTime(null); // ignore
    session2.setDefaults(null); // ignore
    assertEquals(session, session2);
    
    List<String> ids = sessApi.listSessionIds();
    System.out.println(ids);
    
    Map names = sessApi.listSessionNames();
    System.out.println(names);
    
    assertEquals(ids.size(),names.size());
  }

  @Test
  public void testRequestsApi() throws ApiException {
    List<Operation> ops = reqApi.listOperations();
    System.out.println(ops);
    
    try{
      Session session = new Session().id("newSess").label("name 1");
      sessApi.createSession(session);
    }catch(Exception e){
      // ok if it already exists;
    }
    {
      Request req = new Request().sessionId("newSess").label("req1Name")
          .operationId("addSourceDataset");
      Request req2 = reqApi.submitRequest(req);
      req2.setId(null); // ignore
      req2.setCreatedTime(null); // ignore
      req2.setState(null); // ignore
      if (req2.getOperationParams().isEmpty())
        req2.setOperationParams(null); // ignore
      req2.getJobs().clear();; // ignore
      assertEquals(req, req2);
    }    
    System.out.println(sessApi.listSessionNames());
    System.out.println(sessApi.listSessions());
  }

  @Test
  public void testDatasetsApi() throws ApiException {
    String id = "id_example"; // String | dataset ID
    try {
      Dataset result = datasetApi.getDataset(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionsApi#getDataset");
    }

  }
}
