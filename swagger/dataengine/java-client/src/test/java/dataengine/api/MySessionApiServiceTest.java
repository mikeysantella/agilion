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
    MySessionApiServiceTest me = new MySessionApiServiceTest();
    me.testSessionsApi();
    me.testRequestsApi();
  }

  public static ApiClient getApiClient() {
    String BASEPATH = "http://192.168.11.74:8080/server/deelam/DataEngine/0.0.1";
    return new ApiClient().setBasePath(BASEPATH);
  }

  SessionsApi sessApi = new SessionsApi(getApiClient());
  RequestsApi reqApi = new RequestsApi(getApiClient());

  @Test
  public void testSessionsApi() throws ApiException {
    String sessId = "newSess"+System.currentTimeMillis();
    Session session = new Session().id(sessId);
    sessApi.createSession(session);
      
    Session session2 = sessApi.getSession(sessId);
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
      Request req = new Request().sessionId("newSess").label("req1Name");
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
    DatasetsApi apiInstance = new DatasetsApi(getApiClient());
    String id = "id_example"; // String | dataset ID
    try {
      Dataset result = apiInstance.getDataset(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionsApi#getDataset");
    }

  }
}
