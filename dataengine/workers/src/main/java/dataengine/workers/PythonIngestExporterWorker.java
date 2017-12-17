package dataengine.workers;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.workers.AbstractMultiPythonWrapperWorker.SubjobCommandMsg;
import dataengine.workers.AbstractMultiPythonWrapperWorker.SubjobProgress;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;

@Slf4j
public class PythonIngestExporterWorker extends AbstractMultiPythonWrapperWorker {

  public static void main(String[] args) throws Exception {
    String brokerURL = "tcp://localhost:61616";
    Connection connection = MQClient.connect(brokerURL);
    PythonIngestExporterWorker worker = new PythonIngestExporterWorker(null, connection);
    Job job = new Job().id("testJob");
    boolean success = worker.doWork(job);
    log.info("success={}", success);
    Thread.sleep(3000);
    connection.close();
  }

  @Inject
  public PythonIngestExporterWorker(RpcClientProvider<SessionsDB_I> sessDb, Connection connection)
      throws JMSException {
    super(sessDb, connection, OperationConsts.TYPE_INGESTER, "stompworker.pex");
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    return new Operation().level(1).id(this.getClass().getSimpleName()).info(info)
        .addParamsItem(new OperationParam().key(OperationConsts.INPUT_URI).required(true))
        .addParamsItem(new OperationParam().key(OperationConsts.DATA_FORMAT).required(true)
            .description("type and format of data").addPossibleValuesItem(MY_DATA_FORMAT));
  }

  private static final String MY_DATA_FORMAT = "PEOPLE.CSV";

  @Override
  public boolean canDo(Job job) {
    return MY_DATA_FORMAT.equals(job.getParams().get(OperationConsts.DATA_FORMAT));
  }

  protected SubjobCommandMsg createCommandMsg(int cmdIndex) throws JMSException {
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

  
}
