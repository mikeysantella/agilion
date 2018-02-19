package dataengine.workers;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.inject.Inject;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;

/**
 * Ingest CSV into MySQL database table
 */
@Slf4j
public class PythonExportSqlWorker extends AbstractPythonWrapperWorker {

  final Properties props;
  final String sqlConnect;
  
  public PythonExportSqlWorker(RpcClientProvider<SessionsDB_I> sessDb, Connection connection, int deliveryMode, Properties props)
      throws JMSException {
    super(sessDb, connection, deliveryMode, OperationConsts.TYPE_EXPORTER, "workerConf/stompworker.pex");
    this.props=props;
    sqlConnect=props.getProperty("sqlConnect");
    if(sqlConnect==null)
      log.error("sqlConnect not set!");
    
    // populate type2selectCriteria using props
    List<String> dataFormats = PropertiesUtil.splitList(props, "dataFormats", ",");
    for(String dataFormat:dataFormats) {
      List<String> concepts = PropertiesUtil.splitList(props, dataFormat+".exportConcepts", ",");
      for(String concept:concepts) {
        if(!type2selectCriteria.containsKey(concept)) {
          String selectHeader=props.getProperty(concept+".selectHeader");
          if(selectHeader==null)
            throw new IllegalStateException("No setting for "+concept+".selectHeader");
          
          String selectFields=props.getProperty(concept+".selectFields");
          if(selectFields==null)
            throw new IllegalStateException("No setting for "+concept+".selectFields");
          
          String selectDistinct=props.getProperty(concept+".selectDistinct");
          if(selectDistinct==null)
            throw new IllegalStateException("No setting for "+concept+".selectDistinct");
          
          type2selectCriteria.put(concept, new SelectCriteria(selectHeader, selectFields, Boolean.valueOf(selectDistinct)));
        }
      }
    }
    log.info("type2selectCriteria={}", type2selectCriteria);

  }

  private final Map<String,SelectCriteria> type2selectCriteria=new HashMap<>();
//  static {
//    type2selectFields.put("countries", new SelectCriteria(
//        "concat(lastName, ' ', firstName) as personId, citizenship as countryId, citizenship, birthCountry",
//        true));
//  }
  
  @RequiredArgsConstructor
  static class SelectCriteria {
    final String selectHeader;
    final String selectFields;
    final boolean selectDistinct;
  }
  
  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_EXPORTER);
    return new Operation().level(1).id(this.getClass().getSimpleName()).info(info)
        .addParamsItem(new OperationParam().key(OperationConsts.DATASET_ID).required(true)
            .description("input dataset id")
            .valuetype(ValuetypeEnum.STRING))
        .addParamsItem(new OperationParam().key(OperationConsts.OUTPUT_URI).required(true)
            .description("output URI")
            .valuetype(ValuetypeEnum.URI)
            )
        .addParamsItem(new OperationParam().key(OperationConsts.DATA_SCHEMA).required(true)
            .description("concept type to export")
            .valuetype(ValuetypeEnum.ENUM)
            .possibleValues(new ArrayList<>(type2selectCriteria.keySet()))
            );
  }

  @Override
  public boolean canDo(Job job) {
    return "PythonExportSqlWorker".equals(job.getParams().get(OperationConsts.WORKER_NAME)); 
        //&& type2selectCriteria.containsKey(job.getParams().get(OperationConsts.DATA_SCHEMA));
  }

  @Override
  protected Message createCommandMsg(Job job, Dataset inDS, Dataset outDS) throws Exception {
    state.setPercent(2).setMessage("Determined input and output datasets");
    
    UriMySql inUri = UriCodec.parseMySqlUri(inDS.getUri());
    final File outputFile = new File(URI.create(outDS.getUri()));
    if(outputFile.exists())
      throw new IllegalArgumentException("Output file already exists: "+outputFile.getAbsolutePath());
    return createExportPythonMsg(inUri.getDatabaseName(), inUri.getTablename(), 
        outDS.getDataSchema(), outputFile.getAbsolutePath());
  }

  protected Dataset createInputDataset(Job job) throws Exception {
    String dsId=(String) job.getParams().get(OperationConsts.DATASET_ID);
    Dataset inDS = sessDb.rpc().getDataset(dsId).get();
    if(sessDb!=null) {
      CompletableFuture<Void> addInputDsF = sessDb.rpc().addInputDataset(job.getId(), inDS.getId());
      addInputDsF.get(); // make sure these have finished before returning
    }
    return inDS;
  }
  
  protected Dataset createOutputDataset(Job job, Dataset inDS) throws Exception {
    Dataset outDS = new Dataset()
        .uri((String) job.getParams().get(OperationConsts.OUTPUT_URI))
        .dataFormat(OperationConsts.DATA_FORMAT_CSV)
        .dataSchema((String) job.getParams().get(OperationConsts.DATA_SCHEMA))
        .label("exported dataset - "+job.getParams().get(OperationConsts.DATA_SCHEMA));
    if(sessDb!=null) {
      CompletableFuture<Void> addOutputDsF =
          sessDb.rpc().addOutputDataset(outDS, job.getId()).thenAccept((addedOutDs) -> {
            state.getMetrics().put("exported.dataset.id", addedOutDs.getId());
            state.getMetrics().put("exported.dataset.uri", addedOutDs.getUri());
            //sessDb.rpc().setJobParam(job.getId(), OperationConsts.OUTPUT_URI, addedOutDs.getUri());
          });
      addOutputDsF.get(); // make sure these have finished before returning
    }
    return outDS;
  }
  
  Message createExportPythonMsg(String dbname, String tablename,
      String conceptType, String exportCsvPath) throws JMSException {
    log.info("createExportPythonMsg: {} {}", conceptType, exportCsvPath);
    Message message = session.createTextMessage("EXPORT_TO_NEOCSV");
    message.setJMSReplyTo(replyQueue);
    message.setStringProperty("id", "EXPORT_TO_NEOCSV");
    message.setStringProperty("command", "EXPORT_TO_NEOCSV");

    message.setStringProperty("sqlConnect", sqlConnect);
    message.setStringProperty("dbName", dbname);
    message.setStringProperty("tableName", tablename);
    SelectCriteria selectCriteria=type2selectCriteria.get(conceptType);
    if(selectCriteria==null)
      throw new IllegalArgumentException("No SelectCriteria setting for "+conceptType);
    
    message.setStringProperty("selectHeader", selectCriteria.selectHeader);
    message.setStringProperty("selectFields", selectCriteria.selectFields);
    message.setBooleanProperty("selectDistinct", selectCriteria.selectDistinct);
    message.setStringProperty("exportCsvPath", exportCsvPath);
    return message;
  }

}
