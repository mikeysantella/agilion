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
public class IngestMergeExportTest {
  static Logger log=Logger.getLogger("IngestMergeExportTest");
  
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
    IngestMergeExportTest me = new IngestMergeExportTest(baseUri);

    Session sess=me.createSession();
    String sessId = sess.getId();
    //sessId="4f87fa0b-319e-4776-bc09-14e8112cbcca";
    
    Request ingestReq=me.ingestRequest(sess.getId(), null, "testIngestReq");
    String prevReqId=ingestReq.getId();
    
    String neoDirPath="neoDBs/"+System.currentTimeMillis();
    Request mergeTideReq=me.mergeTideRequest(sessId, prevReqId, "testMergeTideReq", neoDirPath);
    prevReqId=mergeTideReq.getId();
    
    me.exportTideRequest(sessId, prevReqId, "testExportTideReq", neoDirPath, 
        "export.graphml", "graphml", null);
    me.exportTideRequest(sessId, prevReqId, "testExportTideReq", neoDirPath, 
        "export.nodelist", "nodelist", "id, first, last, countryId");
    me.exportTideRequest(sessId, prevReqId, "testExportTideReq", neoDirPath, 
        "export.edgelist", "edgelist", null);
  }

  final SessionsApi sessApi;
  final RequestsApi reqsApi;
  final JobsApi jobsApi;
  final DatasetsApi datasetsApi;
  
  public IngestMergeExportTest(String basePathUri) {
    ApiClient apiClient = new ApiClient().setBasePath(basePathUri);
    sessApi = new SessionsApi(apiClient);
    reqsApi = new RequestsApi(apiClient);
    jobsApi = new JobsApi(apiClient);
    datasetsApi = new DatasetsApi(apiClient);
  }


  Session createSession() throws ApiException {
    List<Operation> ops = reqsApi.listOperations();
    //System.out.println(ops);
    
    Session session = new Session().label("SessionIngestMergeExport");
    Session createdSession=sessApi.createSession(session);
    return createdSession;
  }

  public Request ingestRequest(String sessId, List<String> priorRequestIds, String reqLabel) throws ApiException, InterruptedException {
    {
      HashMap<String, Object> ingestToSqlWorkerParamValues = new HashMap<>();
      ingestToSqlWorkerParamValues.put("inputUri", "file:///home/dlam/dev/agilionReal/dataengine/dataio/TIDE_node_attribute_data.csv");
      ingestToSqlWorkerParamValues.put("dataFormat", "TIDE");
      ingestToSqlWorkerParamValues.put("hasHeader", true);
      
      OperationSelectionMap subOperationSelections = new OperationSelectionMap();
      OperationSelection subOp1 = new OperationSelection().id("PythonIngestToSqlWorker").params(ingestToSqlWorkerParamValues);
      subOperationSelections.put(subOp1.getId(), subOp1);
      
      HashMap<String, Object> addSrcDatasetParamValues = new HashMap<>();
      addSrcDatasetParamValues.put("datasetLabel", "TIDE dataset");
      addSrcDatasetParamValues.put("ingesterWorker", subOp1.getId());
      
      Request req = new Request().sessionId(sessId).label(reqLabel)
          .operation(new OperationSelection().id("AddSourceDataset")
              .params(addSrcDatasetParamValues)
              .subOperationSelections(subOperationSelections)
              );
      req.setPriorRequestIds(priorRequestIds);

      Request submittedReq=reqsApi.submitRequest(req);
      String reqId = submittedReq.getId();
      System.out.println("submittedReq= " + submittedReq);
      assertEquals(reqId, submittedReq.getJobs().get(0).getRequestId());
      assertEquals(reqId, submittedReq.getJobs().get(1).getRequestId());
      waitForCompletion(submittedReq);
      
      //Thread.sleep(1000);
      System.out.println(reqsApi.getRequest(submittedReq.getId()));
      return submittedReq;
    }
  }


  public void waitForCompletion(Request submittedReq) throws ApiException, InterruptedException {
    for(Job job:submittedReq.getJobs()){
      job=jobsApi.getJob(job.getId());
      while(job.getProgress().getPercent()<100){
        System.out.println("Waiting for job to complete: "+job);
        Thread.sleep(2000);
        job=jobsApi.getJob(job.getId());
        if(job.getState().toString().equals("failed"))
          throw new RuntimeException("Job failed: "+job.getId());
      }
    }
  }
  
  private Request mergeTideRequest(String sessId, String ingestReqId, String reqLabel, String neoDir) throws ApiException, InterruptedException {
    HashMap<String, Object> mergeDatasetParamValues = new HashMap<>();
    mergeDatasetParamValues.put("inputRequestId", ingestReqId);
    mergeDatasetParamValues.put("dbPath", neoDir);
    
    Request req = new Request().sessionId(sessId).label(reqLabel)
        .operation(new OperationSelection().id("MergeDatasetToNeo").params(mergeDatasetParamValues)
            //.subOperationSelections(subOperationSelections)
            );
    
    List<String> priorRequestIds=new ArrayList<>();
    priorRequestIds.add(ingestReqId);
    req.setPriorRequestIds(priorRequestIds);
    
    Request submittedReq=reqsApi.submitRequest(req);
    waitForCompletion(submittedReq);
    
    System.out.println(reqsApi.getRequest(submittedReq.getId()));
    return submittedReq;
  }

  private Request exportTideRequest(String sessId, String prevReqId, String reqLabel,
      String neoDir, String outputPath, String exportFormat, String propertyColumns) throws ApiException, InterruptedException {
    HashMap<String, Object> neoExportWorkerParamValues = new HashMap<>();
    neoExportWorkerParamValues.put("dbPath", neoDir);
    neoExportWorkerParamValues.put("exportPath", outputPath);
    neoExportWorkerParamValues.put("exportFormat", exportFormat);
    neoExportWorkerParamValues.put("propertyColumns", propertyColumns);
    
    OperationSelectionMap subOperationSelections = new OperationSelectionMap();
    OperationSelection subOp1 = new OperationSelection().id("NeoExporterWorker").params(neoExportWorkerParamValues);
    subOperationSelections.put(subOp1.getId(), subOp1);

    HashMap<String, Object> exportDatasetParamValues = new HashMap<>();
    exportDatasetParamValues.put("exporterWorker", subOp1.getId());
    
    Request req = new Request().sessionId(sessId).label(reqLabel)
        .operation(new OperationSelection().id("ExportDataset")
            .params(exportDatasetParamValues)
            .subOperationSelections(subOperationSelections)
            );
    
    List<String> priorRequestIds=new ArrayList<>();
    priorRequestIds.add(prevReqId);
    req.setPriorRequestIds(priorRequestIds);
    
    Request submittedReq=reqsApi.submitRequest(req);
    waitForCompletion(submittedReq);
    
    System.out.println(reqsApi.getRequest(submittedReq.getId()));
    return submittedReq;
    }

}
