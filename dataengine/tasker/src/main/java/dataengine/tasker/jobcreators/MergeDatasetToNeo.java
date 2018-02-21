package dataengine.tasker.jobcreators;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import dataengine.api.Dataset;
import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.apis.OperationConsts;
import dataengine.apis.OperationWrapper;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.ValidIdUtils;
import jersey.repackaged.com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;

/**
 * Creates 2 jobs:
 * <pre>
 * 1. Python job to SELECT from SQL DB into CSV files
 * 2. Neo4j job to LOAD CSV files into Neo4j DB
 * </pre>
 */
@Slf4j
//@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class MergeDatasetToNeo extends AbstractJobCreator {

  private final RpcClientProvider<SessionsDB_I> sessDb;
  final Properties configMap;

  @Inject
  public MergeDatasetToNeo(RpcClientProvider<SessionsDB_I> sessionDb, Properties props){
     sessDb=sessionDb;
     configMap=props;
     final File exportDir=new File(configMap.getProperty("exportDir"));
     if(!exportDir.exists()){
       if (!exportDir.mkdirs())
         throw new RuntimeException("Could not create directory: "+exportDir.getAbsolutePath());
		 exportDir.setExecutable(true, false);
       exportDir.setReadable(true, false);
       exportDir.setWritable(true, false);
     }
  }
 
  @Override
  protected Operation initOperation() {
    Operation operation = new Operation().level(0).id(this.getClass().getSimpleName())
        .description("export SQL datasets and merge into Neo4j database")
        .info(new HashMap<>());

    operation.addParamsItem(new OperationParam().key(OperationConsts.INPUT_REQUESTID)
        .required(true)
        .description("request id that created the dataset to be merged")
        .valuetype(ValuetypeEnum.STRING));

//    operation.addParamsItem(new OperationParam().key(OperationConsts.DATA_FORMAT)
//        .required(true)
//        .description("type and format of data")
//        .valuetype(ValuetypeEnum.ENUM));
    
//    operation.addParamsItem(new OperationParam().key(OperationConsts.DATASET_LABEL)
//        .required(true)
//        .description("label for input dataset")
//        .valuetype(ValuetypeEnum.STRING));
    
    operation.addParamsItem(new OperationParam().key(OperationConsts.DB_PATH)
        .required(true)
        .description("path for output Neo4j DB")
        .valuetype(ValuetypeEnum.STRING));

    return operation;
  }

  public void updateOperationParams(Map<String, Operation> currOperations) {
    // copy ingester-type ops from workers
//    List<Operation> ingesterOps = copyOperationsOfType(currOperations, OperationConsts.TYPE_INGESTER);
//    opW = new OperationWrapper(initOperation(), ingesterOps);

//    opW.getOperationParam(OperationConsts.INGESTER_WORKER).possibleValues(
//        ingesterOps.stream().map(Operation::getId).collect(toList()));

    opW.operationUpdated();
  }


  @SuppressWarnings("unchecked")
  @Override
  public List<JobEntry> createFrom(final Request req, List<String> priorJobIds) {
    OperationSelection selection = req.getOperation();
    checkArgument(opW.getOperation().getId().equals(selection.getId()), "Operation.id does not match!");
    opW.convertParamValues(selection);
    
    String inputReqId=(String) selection.getParams().get(OperationConsts.INPUT_REQUESTID);
    List<Dataset> inputDatasets = sessDb.rpc().getRequestOutputDatasets(inputReqId).join();
    if(inputDatasets.size()!=1) {
      throw new IllegalStateException("Expecting only 1 dataset but got: "+inputDatasets);
    }
    Dataset inputDataset = inputDatasets.get(0);
    
    List<JobEntry> jobEntries=new ArrayList<>();
    
    final String jobPrefix = genJobIdPrefix(req);
    Job job0 = new Job().id(jobPrefix + ".job0-preRequest")
        .type(OperationConsts.TYPE_PREREQUEST)
        .requestId(req.getId())
        .label("Pre-request job for: " + req.getLabel());    
    jobEntries.add(new JobEntry(job0, priorJobIds.toArray(new String[priorJobIds.size()])));
    
    String conceptsToExport = (String) inputDataset.getStats().get(OperationConsts.EXPORT_CONCEPTS);
    if(conceptsToExport==null)
      throw new IllegalStateException("Dataset does not have "+OperationConsts.EXPORT_CONCEPTS+
          " stats property");
    List<String> concepts = PropertiesUtil.splitList(",", conceptsToExport);
    
    Map<String, URI> csvFiles=new HashMap<>();
    Job prevJob=job0;
    final File exportDir=new File(configMap.getProperty("exportDir"), inputDataset.getDataSchema()+"-forNeo-"+System.currentTimeMillis());
    if(!exportDir.exists() && !exportDir.mkdirs())
      throw new RuntimeException("Could not create directory: "+exportDir.getAbsolutePath());
    for(String concept:concepts){
      Job nextJob = new Job().id(jobPrefix + ".job1-exportToCsvFiles."+ValidIdUtils.makeValid(concept))
          .type(OperationConsts.TYPE_EXPORTER)
          .requestId(req.getId())
          .label("Export "+ concept +" csv from "+ inputDataset.getLabel());
      
      {
        Map<String, Object> job1Params = new HashMap<>(selection.getParams());
        job1Params.put(OperationConsts.WORKER_NAME, "PythonExportSqlWorker");
        job1Params.put(OperationConsts.DATASET_ID, inputDataset.getId());
        URI outputUri=new File(exportDir, concept+".csv").toURI().normalize();
        job1Params.put(OperationConsts.OUTPUT_URI, outputUri.toString());
        job1Params.put(OperationConsts.DATA_SCHEMA, inputDataset.getDataSchema()+"."+concept); // concept to export
        csvFiles.put(concept,outputUri);
        nextJob.params(job1Params);
      }
      
      jobEntries.add(new JobEntry(nextJob, prevJob.getId()));
      prevJob=nextJob;
    }

    log.info("csvFiles={}", csvFiles);
    Job job2 = new Job().id(jobPrefix + ".job2-loadCsvToNeo")
        .type(OperationConsts.TYPE_IMPORTER)
        .requestId(req.getId())
        .label("LoadCsvToNeo from " + inputDataset.getLabel());
    {
      Map<String, Object> job2Params = new HashMap<>(selection.getParams());
      //job2Params.put(OperationConsts.PREV_JOBID, prevJob.getId());
      job2Params.put(OperationConsts.WORKER_NAME, "CsvToNeoWorker");
      //job2Params.put(OperationConsts.DATA_FORMAT, OperationConsts.DATA_FORMAT_CSV);
      job2Params.put(OperationConsts.DATA_SCHEMA, inputDataset.getDataSchema());
      job2Params.put(OperationConsts.DB_PATH, selection.getParams().get(OperationConsts.DB_PATH));
      
      job2Params.putAll(csvFiles);
      job2.params(job2Params);
    }
    jobEntries.add(new JobEntry(job2, prevJob.getId()));
    
    Job job3 = new Job().id(jobPrefix + ".job3-postRequest")
        .type(OperationConsts.TYPE_POSTREQUEST)
        .requestId(req.getId())
        .label("Post-request job for: " + req.getLabel());
    {
      Map<String, Object> job3Params = new HashMap<>(selection.getParams());
      job3Params.put(OperationConsts.JOBID_OF_OUTPUT_DATASET, job2.getId());
      job3.params(job3Params);
    }
    jobEntries.add(new JobEntry(job3, job2.getId()));

    return jobEntries;
  }
}
