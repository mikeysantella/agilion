package dataengine.api;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
    String baseUri = "http://localhost:9090/main/";
    if(args.length>0)
      baseUri=args[0];
    if(!baseUri.startsWith("http")){
      baseUri = "http://"+baseUri+":9090/main/";
    }
    if(!baseUri.contains("DataEngine"))
      baseUri += "DataEngine/0.0.3";
    System.out.println("Using baseUri="+baseUri);
    MySessionApiServiceTest me = new MySessionApiServiceTest(baseUri);
    me.testSessionsApi();
    
    List<String> priorReqIds=new ArrayList<>();
    Request req1=me.testRequestsApi(null);   
    priorReqIds.add(req1.getId());
    Request req2=me.testRequestsApi(priorReqIds);
    priorReqIds.add(req2.getId());
    Request req3=me.testRequestsApi(priorReqIds);
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
    
    System.out.println(sessApi.listSessions());
    Map names = sessApi.listSessionNames();
    System.out.println(names);
    
    assertEquals(ids.size(),names.size());
  }

  @Test
  public Request testRequestsApi(List<String> priorRequestIds) throws ApiException, InterruptedException {
    List<Operation> ops = reqsApi.listOperations();
    System.out.println(ops);
    
    try{
      Session session = new Session().id("newSess").label("name 1");
      sessApi.createSession(session);
    }catch(Exception e){
      // ok if it already exists;
    }
    {
      HashMap<String, Object> ingestTelephoneParamValues = new HashMap<>();
      ingestTelephoneParamValues.put("workTime", "10");
      ingestTelephoneParamValues.put("dataFormat", "TELEPHONE.CSV");
      boolean sendAsString=false;
      if(sendAsString) {
        ingestTelephoneParamValues.put("inputEdgelistUris", "");
        ingestTelephoneParamValues.put("inputNodelistUris", 
            new File("README.md").toURI().toASCIIString()+","+new File("README2.md").toURI().toASCIIString());
      } else {
        ingestTelephoneParamValues.put("inputEdgelistUris", Collections.emptyList());
        ArrayList<String> nodeList = new ArrayList<String>();
        nodeList.add(new File("README3.md").toURI().toASCIIString());
        nodeList.add(new File("README4.md").toURI().toASCIIString());
        ingestTelephoneParamValues.put("inputNodelistUris",nodeList);
      }
      
      OperationSelectionMap subOperationSelections = new OperationSelectionMap();
      OperationSelection subOp1 = new OperationSelection().id("IngestTelephoneDummyWorker").params(ingestTelephoneParamValues);
      subOperationSelections.put(subOp1.getId(), subOp1);
      
      HashMap<String, Object> addSrcDatasetParamValues = new HashMap<>();
      addSrcDatasetParamValues.put("datasetLabel", "TELEPHONE_TEST_DATASET");
      addSrcDatasetParamValues.put("ingesterWorker", subOp1.getId());
      
      Request req = new Request().sessionId("newSess").label("req1Name")
          .operation(new OperationSelection().id("AddSourceDataset").params(addSrcDatasetParamValues)
              .subOperationSelections(subOperationSelections)
              );
      req.setPriorRequestIds(priorRequestIds);

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
          if(job.getProgress().getPercent()<0)
            throw new RuntimeException("Job failed: "+job.getId());
        }
      }
      
      
      Thread.sleep(2000);
      System.out.println(reqsApi.getRequest(req3.getId()));
      System.out.println(sessApi.listSessionNames());
      System.out.println(sessApi.listSessions());
      System.out.println(reqsApi.listOperations());
      
      return req3;
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
