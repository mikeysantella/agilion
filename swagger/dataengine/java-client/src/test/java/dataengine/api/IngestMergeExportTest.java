package dataengine.api;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.junit.Ignore;
import dataengine.ApiClient;
import dataengine.ApiException;

@Ignore
public class IngestMergeExportTest {
  static Logger log=Logger.getLogger("IngestMergeExportTest");
  
  public static void main(String[] args) throws Exception {
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

    String neoDirPath=new File("neoDBs/"+System.currentTimeMillis()).getAbsolutePath();
    String prevReqId=null;
    if(true){
      Request ingestTideReq=me.ingestRequest(sess.getId(), null, "testIngestTideReq",
          new File("../../../dataengine/dataio/TIDE_node_attribute_data.csv").toURI().normalize(),
          "TIDE");
  
      Request mergeTideReq=me.mergeRequest(sessId, ingestTideReq.getId(), "testMergeTideReq", neoDirPath);
      prevReqId=mergeTideReq.getId();
    }

    if(true){
      Request ingestI94Req=me.ingestRequest(sess.getId(), null, "testIngestI94Req",
          new File("../../../dataengine/dataio/I-94_node_attribute_data.csv").toURI().normalize(),
        "I94Visa");
      
      Request mergeI94Req=me.mergeRequest(sessId, ingestI94Req.getId(), "testMergeI94Req", neoDirPath);
      prevReqId=mergeI94Req.getId();
    }
    
    if(true){
      Request ingestFbReq=me.ingestRequest(sess.getId(), null, "testIngestFbReq",
          new File("../../../dataengine/dataio/Facebook_node_attribute_data.csv").toURI().normalize(),
        "FB");
      
      Request mergeFbReq=me.mergeRequest(sessId, ingestFbReq.getId(), "testMergeFbReq", neoDirPath);
      prevReqId=mergeFbReq.getId();

      if(true){ // if an endpoint of an edge does not match, no edge is created, so must import nodes -- see above 
        Request ingestFbEdgesReq=me.ingestRequest(sess.getId(), null, "testIngestFbEdgesReq",
            new File("../../../dataengine/dataio/Facebook_edgelist_data.csv").toURI().normalize(),
          "FBEdges");
        
        Request mergeFbEdgesReq=me.mergeRequest(sessId, ingestFbEdgesReq.getId(), "testMergeFbEdgesReq", neoDirPath);
        prevReqId=mergeFbEdgesReq.getId();
      }
    }
    
    if(true){
      Request ingestCdrReq=me.ingestRequest(sess.getId(), null, "testIngestCdrReq",
          new File("../../../dataengine/dataio/CDR_node_attribute_data.csv").toURI().normalize(),
        "CDR");
      
      Request mergeCdrReq=me.mergeRequest(sessId, ingestCdrReq.getId(), "testMergeCdrReq", neoDirPath);
      prevReqId=mergeCdrReq.getId();

      if(true){ // if an endpoint of an edge does not match, no edge is created, so must import nodes -- see above 
        Request ingestCdrEdgesReq=me.ingestRequest(sess.getId(), null, "testIngestCdrEdgesReq",
            new File("../../../dataengine/dataio/CDR_edgelist_data.csv").toURI().normalize(),
          "CDREdges");
        
        Request mergeCdrEdgesReq=me.mergeRequest(sessId, ingestCdrEdgesReq.getId(), "testMergeCdrEdgesReq", neoDirPath);
        prevReqId=mergeCdrEdgesReq.getId();
      }
    }
    
    {
      me.exportRequest(sessId, prevReqId, "testExportGraphmlReq", neoDirPath, 
          "export.graphml", "graphml", null);
      me.exportRequest(sessId, prevReqId, "testExportNodelistReq", neoDirPath, 
          "export.nodelist", "nodelist", "nodeId, nodeType, nameFirst, nameLast, phoneMsisdn, countryCitizen");
      me.exportRequest(sessId, prevReqId, "testExportEdgelistReq", neoDirPath, 
          "export.edgelist", "edgelist", null);
    }
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

  public Request ingestRequest(String sessId, List<String> priorRequestIds, String reqLabel,
      URI inputUri, String dataSchema) throws ApiException, InterruptedException, FileNotFoundException {
    {
      if(!new File(inputUri).exists())
        throw new FileNotFoundException("Copy input file to this location: "+inputUri);
      
      HashMap<String, Object> ingestToSqlWorkerParamValues = new HashMap<>();
      ingestToSqlWorkerParamValues.put("inputUri", inputUri.toString());
      ingestToSqlWorkerParamValues.put("dataSchema", dataSchema);
      ingestToSqlWorkerParamValues.put("hasHeader", true);
      
      OperationSelectionMap subOperationSelections = new OperationSelectionMap();
      OperationSelection subOp1 = new OperationSelection().id("PythonIngestToSqlWorker").params(ingestToSqlWorkerParamValues);
      subOperationSelections.put(subOp1.getId(), subOp1);
      
      HashMap<String, Object> addSrcDatasetParamValues = new HashMap<>();
      addSrcDatasetParamValues.put("datasetLabel", dataSchema+" dataset");
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
  
  private Request mergeRequest(String sessId, String ingestReqId, String reqLabel, String neoDir) throws ApiException, InterruptedException {
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

  private Request exportRequest(String sessId, String prevReqId, String reqLabel,
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
