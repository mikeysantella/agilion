package dataengine.workers;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import dataengine.api.Dataset;
import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.Request;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.UriCodec;
import dataengine.apis.UriCodec.UriMySql;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;

@Slf4j
public class PythonIngesterWorker extends AbstractPythonWrapperWorker {

  public static void main(String[] args) throws Exception {
    String brokerURL = "tcp://localhost:61616";
    Connection connection = MQClient.connect(brokerURL);
    AbstractPythonWrapperWorker worker = new PythonIngesterWorker(null, connection);
    Job job = new Job().id("testJob");
    boolean success = worker.doWork(job);
    log.info("success={}", success);
    Thread.sleep(3000);
    connection.close();
  }

  @Inject
  public PythonIngesterWorker(RpcClientProvider<SessionsDB_I> sessDb, Connection connection)
      throws JMSException {
    super(sessDb, connection, OperationConsts.TYPE_INGESTER, "workerConf/stompworker.pex");
    type2Conf.values().forEach(confFile->{
      if(!new File(confFile).exists())
        log.warn("File does not exist: {}", confFile);
    });
  }

  private static final Map<String,String> type2Conf=new HashMap<>();
  static {
    type2Conf.put("TIDE", "workerConf/fieldmap.TIDE.conf");
    type2Conf.put("I94Visa", "workerConf/fieldmap.I94Visa.conf");
  }
  
  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    return new Operation().level(1).id(this.getClass().getSimpleName()).info(info)
        .addParamsItem(new OperationParam().key(OperationConsts.INPUT_URI).required(true))
        .addParamsItem(new OperationParam().key(OperationConsts.DATA_FORMAT).required(true)
            .description("type and format of data")
            .possibleValues(new ArrayList<>(type2Conf.keySet()))
            );
  }

  @Override
  public boolean canDo(Job job) {
    return type2Conf.containsKey(job.getParams().get(OperationConsts.DATA_FORMAT));
  }

  private String domainfieldsFile="workerConf/domainfields.intel.conf";

  @Override
  protected Message createCommandMsg(Job job, Dataset inDS, Dataset outDS) throws Exception {
    state.setPercent(2).setMessage("Determined input and output datasets");
    
    if(true) {
      String fieldmapFile = type2Conf.get(inDS.getDataFormat());
      UriMySql outUri = UriCodec.parseMySqlUri(outDS.getUri());
      return createGetQueryPythonMsg(domainfieldsFile, fieldmapFile, 
          outUri.getDatabaseName(), outUri.getTablename(), inDS.getUri());
    }else {
      // TODO: get params from Job
      return createGetQueryPythonMsg("/home/dlam/dev/mysql-ingest/domainfields.intel.conf",
          "/home/dlam/dev/mysql-ingest/fieldmap.TIDE.conf", "thegeekstuff",
          "table" + System.currentTimeMillis(), "/tmp/data/INTEL_datasets/TIDE_sample_data.csv");
    }
  }

  protected Dataset createInputDataset(Job job) throws Exception {
    Dataset inDS = new Dataset()
        .uri((String) job.getParams().get(OperationConsts.INPUT_URI))
        .dataFormat((String) job.getParams().get(OperationConsts.DATA_FORMAT))
        .label("input for job " + job.getId());
    CompletableFuture<Dataset> addInputDsF = sessDb.rpc().addInputDataset(inDS, job.getId());
    addInputDsF.get(); // make sure these have finished before returning
    return inDS;
  }
  
  protected Dataset createOutputDataset(Job job, Dataset inDS) throws Exception {
    String sessId = sessDb.rpc().getRequest(job.getRequestId()).thenApply(Request::getSessionId).get();
    String tablename = inDS.getDataFormat() + "-" + System.currentTimeMillis();
    String outDsUri=UriCodec.genMySqlUri(sessId, tablename);
    Dataset outDS = new Dataset()
        .uri(outDsUri)
        .dataFormat(OperationConsts.DATA_FORMAT_PARQUET)
        .label("ingested dataset");
    CompletableFuture<Void> addOutputDsF =
        sessDb.rpc().addOutputDataset(outDS, job.getId()).thenAccept((addedOutDs) -> {
          state.getMetrics().put("ingested.dataset.id", addedOutDs.getId());
          state.getMetrics().put("ingested.dataset.uri", addedOutDs.getUri());
          sessDb.rpc().setJobParam(job.getId(), OperationConsts.OUTPUT_URI, addedOutDs.getId());
        });
    addOutputDsF.get(); // make sure these have finished before returning
    return outDS;
  }

  Message createGetQueryPythonMsg(String domainfieldsFile, String fieldmapFile, String dbname,
      String tablename, String csvFile) throws JMSException {
    log.info("sendPythonRequest: {}", domainfieldsFile);
    Message message = session.createTextMessage("QUERY");
    message.setJMSReplyTo(replyQueue);
    message.setStringProperty("id", "QUERY");
    message.setStringProperty("command", "QUERY");

    message.setStringProperty("domainfieldsFile", domainfieldsFile);
    message.setStringProperty("fieldmapFile", fieldmapFile);
    message.setStringProperty("dbname", dbname);
    message.setStringProperty("tablename", tablename);
    message.setStringProperty("csvFile", csvFile);
    // message.setStringProperty("dbUri", outDS.getUri());
    return message;
  }

}
