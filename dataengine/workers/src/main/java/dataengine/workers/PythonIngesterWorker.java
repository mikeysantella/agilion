package dataengine.workers;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import dataengine.api.Dataset;
import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.api.Request;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.UriCodec;
import dataengine.apis.UriCodec.UriMySql;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;

/**
 * Ingest CSV into MySQL database table
 */
@Slf4j
public class PythonIngesterWorker extends AbstractPythonWrapperWorker {

  public static void main(String[] args) throws Exception {
    String brokerURL = "tcp://localhost:61616";
    Connection connection = MQClient.connect(brokerURL);
    
    Properties deProps = PropertiesUtil.loadProperties("../main/dataengine.props");
    Properties props=new Properties();
    String prefix="workers.";
    props.put("sqlConnect", deProps.get(prefix+"sqlConnect"));
    props.put("TIDE.dshape", deProps.get(prefix+"TIDE.dshape"));
    props.put("I94Visa.dshape", deProps.get(prefix+"I94Visa.dshape"));
    
    AbstractPythonWrapperWorker worker = new PythonIngesterWorker(null, connection, DeliveryMode.NON_PERSISTENT, props);
    
    Map<String, Object> params=new HashMap<>();
    int testCase=3;
    switch(testCase) {
      case 1:
        // This test file is not consistent with case 2!!
        params.put((OperationConsts.INPUT_URI), "file:///home/dlam/dev/agilionReal/dataengine/dataio/INTEL_datasets/TIDE_sample_data.csv");
        params.put((OperationConsts.DATA_FORMAT), "TIDE");
        break;
      case 2:
        params.put((OperationConsts.INPUT_URI), "file:///home/dlam/dev/agilionReal/dataengine/dataio/TIDE_node_attribute_data.csv");
        params.put((OperationConsts.DATA_FORMAT), "TIDE");
        break;
      case 3:
        params.put((OperationConsts.INPUT_URI), "file:///home/dlam/dev/agilionReal/dataengine/dataio/I-94_node_attribute_data.csv");
        params.put((OperationConsts.DATA_FORMAT), "I94Visa");
        break;
    }
    
    Job job = new Job().id("testJob").params(params);
    boolean success = worker.doWork(job);
    log.info("success={}", success);
    Thread.sleep(3000);
    connection.close();
  }

  final Properties props;
  final String sqlConnect;
  
  public PythonIngesterWorker(RpcClientProvider<SessionsDB_I> sessDb, Connection connection, int deliveryMode, Properties props)
      throws JMSException {
    super(sessDb, connection, deliveryMode, OperationConsts.TYPE_INGESTER, "workerConf/stompworker.pex");
    this.props=props;
    sqlConnect=props.getProperty("sqlConnect");
    if(sqlConnect==null)
      log.error("sqlConnect not set!");
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
        .addParamsItem(new OperationParam().key(OperationConsts.HAS_HEADER).required(true)
            .description("whether input file has a header")
            .valuetype(ValuetypeEnum.BOOLEAN).defaultValue(true))
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
      UriMySql outUri = UriCodec.parseMySqlUri(outDS.getUri());
      final File inputFile = new File(URI.create(inDS.getUri()));
      if(!inputFile.exists())
        throw new FileNotFoundException(inputFile.getAbsolutePath());
      String hasHeaderStr=(String) inDS.getStats().get(OperationConsts.HAS_HEADER);
      boolean inputDSHasHeader=(hasHeaderStr==null)?true:Boolean.valueOf(hasHeaderStr);
      return createIngestPythonMsg(outUri.getDatabaseName(), outUri.getTablename(), 
          inputFile.getAbsolutePath(), inDS.getDataFormat(), inputDSHasHeader);
    } else if(true){
      String fieldmapFile = type2Conf.get(inDS.getDataFormat());
      UriMySql outUri = UriCodec.parseMySqlUri(outDS.getUri());
      return createGetQueryPythonMsg(domainfieldsFile, fieldmapFile, 
          outUri.getDatabaseName(), outUri.getTablename(), inDS.getUri());
    } else {
      // TODO: get params from Job
      return createGetQueryPythonMsg("/home/dlam/dev/mysql-ingest/domainfields.intel.conf",
          "/home/dlam/dev/mysql-ingest/fieldmap.TIDE.conf", "thegeekstuff",
          "table" + System.currentTimeMillis(), "/tmp/data/INTEL_datasets/TIDE_sample_data.csv");
    }
  }

  protected Dataset createInputDataset(Job job) throws Exception {
    final HashMap<String, Object> statsMap = new HashMap<>();
    statsMap.put(OperationConsts.HAS_HEADER, job.getParams().get(OperationConsts.HAS_HEADER));
    
    Dataset inDS = new Dataset()
        .uri((String) job.getParams().get(OperationConsts.INPUT_URI))
        .dataFormat((String) job.getParams().get(OperationConsts.DATA_FORMAT))
        .stats(statsMap)
        .label("input for job " + job.getId());
    if(sessDb!=null) {
      CompletableFuture<Dataset> addInputDsF = sessDb.rpc().addInputDataset(inDS, job.getId());
      addInputDsF.get(); // make sure these have finished before returning
    }
    return inDS;
  }
  
  protected Dataset createOutputDataset(Job job, Dataset inDS) throws Exception {
    String sessId = (sessDb==null)?"sess123":sessDb.rpc().getRequest(job.getRequestId()).thenApply(Request::getSessionId).get();
    String tablename = inDS.getDataFormat() + "___" + System.currentTimeMillis();
    String outDsUri=UriCodec.genMySqlUri(sessId, tablename);
    Dataset outDS = new Dataset()
        .uri(outDsUri)
        .dataFormat(OperationConsts.DATA_FORMAT_PARQUET)
        .label("ingested dataset");
    if(sessDb!=null) {
      CompletableFuture<Void> addOutputDsF =
          sessDb.rpc().addOutputDataset(outDS, job.getId()).thenAccept((addedOutDs) -> {
            state.getMetrics().put("ingested.dataset.id", addedOutDs.getId());
            state.getMetrics().put("ingested.dataset.uri", addedOutDs.getUri());
            sessDb.rpc().setJobParam(job.getId(), OperationConsts.OUTPUT_URI, addedOutDs.getId());
          });
      addOutputDsF.get(); // make sure these have finished before returning
    }
    return outDS;
  }

  Message createGetQueryPythonMsg(String domainfieldsFile, String fieldmapFile, String dbname,
      String tablename, String csvFile) throws JMSException {
    log.info("createGetQueryPythonMsg: {}", domainfieldsFile);
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
  
  Message createIngestPythonMsg(String dbname, String tablename,
      String csvFile, String csvFormat, boolean hasHeader) throws JMSException {
    log.info("createIngestPythonMsg: {} {}", csvFile, csvFormat);
    Message message = session.createTextMessage("INGEST");
    message.setJMSReplyTo(replyQueue);
    message.setStringProperty("id", "INGEST");
    message.setStringProperty("command", "INGEST");

    message.setStringProperty("sqlConnect", sqlConnect);
    message.setStringProperty("dbName", dbname);
    message.setStringProperty("tableName", tablename);
    message.setStringProperty("sourcedataCsv", csvFile);
    
    String dshape=props.getProperty(csvFormat+".dshape");
    if(dshape==null)
      log.error("No setting for ", csvFormat+".dshape");
    message.setStringProperty("dshape", dshape);
    message.setBooleanProperty(OperationConsts.HAS_HEADER, hasHeader);
    //message.setStringProperty("exportDir", exportDir);
    return message;
  }

}
