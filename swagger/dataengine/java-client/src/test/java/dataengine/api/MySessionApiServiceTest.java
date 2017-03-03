package dataengine.api;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;

import dataengine.ApiClient;
import dataengine.ApiException;

@Ignore
public class MySessionApiServiceTest {
  static Logger log=Logger.getLogger("MySessionApiServiceTest");
  
  public static void main(String[] args) throws ApiException, InterruptedException {
    String baseUri = "http://localhost:8080/main/";
    if(args.length>0)
      baseUri=args[0];
    if(!baseUri.startsWith("http")){
      baseUri = "http://"+baseUri+":8080/main/";
    }
    if(!baseUri.contains("DataEngine"))
      baseUri += "DataEngine/0.0.2";
    System.out.println("Using baseUri="+baseUri);
    MySessionApiServiceTest me = new MySessionApiServiceTest(baseUri);
    me.testSessionsApi();
    me.testRequestsApi();
  }

  final SessionsApi sessApi;
  final RequestsApi reqsApi;
  final JobsApi jobsApi;
  final DatasetsApi datasetsApi;
  
  public MySessionApiServiceTest(String basePathUri) {
    ApiClient apiClient = new ApiClient().setBasePath(basePathUri);
    sessApi = new SessionsApi(apiClient);
    reqsApi = new RequestsApi(apiClient);
    jobsApi = new JobsApi(apiClient);
    datasetsApi = new DatasetsApi(apiClient);
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
  public void testRequestsApi() throws ApiException, InterruptedException {
    List<Operation> ops = reqsApi.listOperations();
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
      HashMap<String, Object> paramValues = new HashMap<String, Object>();
      req.operationParams(paramValues);
      paramValues.put("inputUri", "hdfs://some/where/");
      paramValues.put("dataFormat", "TELEPHONE.CSV");

      Request req2 = reqsApi.submitRequest(req);
      String reqId = req2.getId();
//      req2.setId(null); // ignore
//      req2.setCreatedTime(null); // ignore
//      req2.setState(null); // ignore
//      if (req2.getOperationParams().isEmpty())
//        req2.setOperationParams(null); // ignore
//      req2.getJobs().clear();; // ignore
//      assertEquals(req, req2);
      
      Request req3=req2;
      System.out.println("req3= " + req3);
      assertEquals(reqId, req3.getJobs().get(0).getRequestId());
      assertEquals(reqId, req3.getJobs().get(1).getRequestId());
      for(Job job:req3.getJobs()){
        job=jobsApi.getJob(job.getId());
        while(job.getProgress().getPercent()<100){
          System.out.println("Waiting for job to complete: "+job);
          Thread.sleep(2000);
          job=jobsApi.getJob(job.getId());
        }
      }
      System.out.println(sessApi.listSessionNames());
      System.out.println(sessApi.listSessions());
    }
  }

  @Test
  public void testDatasetsApi() throws ApiException {
    String id = "id_example"; // String | dataset ID
    try {
      Dataset result = datasetsApi.getDataset(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SessionsApi#getDataset");
    }

  }
}
