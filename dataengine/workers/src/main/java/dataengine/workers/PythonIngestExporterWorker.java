package dataengine.workers;

import java.util.HashMap;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import dataengine.api.Dataset;
import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;

@Slf4j
public class PythonIngestExporterWorker extends AbstractMultiPythonWrapperWorker {

  public static void main(String[] args) throws Exception {
    String brokerURL = "tcp://localhost:61616";
    Connection connection = MQClient.connect(brokerURL);
    PythonIngestExporterWorker worker = new PythonIngestExporterWorker(null, connection, DeliveryMode.NON_PERSISTENT);
    Job job = new Job().id("testJob");
    boolean success = worker.doWork(job);
    log.info("success={}", success);
    Thread.sleep(3000);
    connection.close();
  }

  public PythonIngestExporterWorker(RpcClientProvider<SessionsDB_I> sessDb, Connection connection, int deliveryMode)
      throws JMSException {
    super(sessDb, connection, deliveryMode, OperationConsts.TYPE_EXPORTER, "workerConf/stompworker.pex");
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_EXPORTER);
    return new Operation().level(1).id(this.getClass().getSimpleName()).info(info)
        .addParamsItem(new OperationParam().key(OperationConsts.INPUT_URI).required(true)
            .valuetype(ValuetypeEnum.URI))
        .addParamsItem(new OperationParam().key(OperationConsts.DATA_FORMAT).required(true)
            .description("type and format of data")
            .valuetype(ValuetypeEnum.ENUM)
            .addPossibleValuesItem(MY_DATA_FORMAT));
  }

  private static final String MY_DATA_FORMAT = "PEOPLE.CSV";

  @Override
  public boolean canDo(Job job) {
    return MY_DATA_FORMAT.equals(job.getParams().get(OperationConsts.DATA_FORMAT));
  }

  protected SubjobCommandMsg createCommandMsg(int cmdIndex, Dataset inDS, Dataset outDS) throws JMSException {
    switch(cmdIndex) {
      case 0:
        // TODO: get params from Job
        return new SubjobCommandMsg(new SubjobProgress(50), 
            createGetQueryPythonMsg("/home/dlam/dev/mysql-ingest/domainfields.intel.conf",
            "/home/dlam/dev/mysql-ingest/fieldmap.TIDE.conf", "thegeekstuff",
            "table" + System.currentTimeMillis(), "/tmp/data/INTEL_datasets/TIDE_sample_data.csv"));
      case 1:
        return new SubjobCommandMsg(new SubjobProgress(50), 
            createSelectSqlPythonMsg("root", "my-secret-pw", "127.0.0.1",
            "thegeekstuff", "table1513392921404"));
    }
    return null;
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
    // message.setStringProperty("methodName", "meth1");
    return message;
  }

  Message createSelectSqlPythonMsg(String user, String pw, String host, String dbname,
      String tablename) throws JMSException {
    log.info("createSelectSqlPythonMsg: {}", user);
    Message message = session.createTextMessage("SELECT");
    message.setJMSReplyTo(replyQueue);
    message.setStringProperty("id", "SELECT");
    message.setStringProperty("command", "SELECT");
    message.setStringProperty("user", user);
    message.setStringProperty("pw", pw);
    message.setStringProperty("db", dbname);
    message.setStringProperty("host", host);
    message.setStringProperty("tablename", tablename);
    return message;
  }

  @Override
  protected Dataset createOutputDataset(Job job, Dataset inDS) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Dataset createInputDataset(Job job) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  
}
