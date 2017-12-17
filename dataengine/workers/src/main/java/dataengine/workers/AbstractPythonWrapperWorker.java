package dataengine.workers;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import dataengine.api.Job;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.RuntimeUtils;

@Slf4j
//@Accessors(fluent = true)
public abstract class AbstractPythonWrapperWorker extends BaseWorker<Job> {

  protected static final String END = "END";

  protected final Session session;
  protected final MessageProducer producer;
  protected final Queue replyQueue;

  final String pythonExecFile;
  final String pythonQueueName;
  final Destination pythonDestQ;

  public AbstractPythonWrapperWorker(RpcClientProvider<SessionsDB_I> sessDb, Connection connection,
      String jobType, String pythonExecFile) throws JMSException {
    super(jobType, sessDb);
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    this.pythonExecFile = pythonExecFile;

    if (!new File(pythonExecFile).exists()) {
      throw new IllegalArgumentException("Python file doesn't exist: " + pythonExecFile);
    }

    replyQueue = session.createQueue(name()+".pythonStatusQ");
    this.pythonQueueName = name()+".pythonCommandQ";
    pythonDestQ = session.createQueue(pythonQueueName);
    MQClient.createQueueConsumer(session, replyQueue.getQueueName(), m -> {
      try {
        if (END.equals(m.getStringProperty("id")))
          return;
        int percent = m.getIntProperty("percent");
        String message = m.getStringProperty("message");
        if (pythonCompleteF.isDone())
          log.warn("After job was done, received python status msg: {}; {}; {}", percent, message, m);
        else
          log.info("Received python status msg: {}; {}; {}", percent, message, m);
        
        onPythonReply(m, percent, message);
      }catch(RuntimeException e){
        log.warn("When handling python progress", e);
      }
    });
    producer = MQClient.createGenericMsgResponder(session, DeliveryMode.NON_PERSISTENT);
  }

  protected void onPythonReply(Message m, int percent, String message) throws JMSException {
    state.setPercent(percent).setMessage(message);
    updateFuture(percent, pythonCompleteF);
  }

  static void updateFuture(int percent, CompletableFuture<Boolean> completeF) {
    if(percent<0) {
      completeF.complete(false);
    } else if(percent>=100) {
      completeF.complete(true);
    }
  }
  
  protected String pythonExecutable = "python3";
  protected CompletableFuture<Boolean> pythonCompleteF;

  @Override
  protected boolean doWork(Job job) throws Exception {
    pythonCompleteF = new CompletableFuture<>();

    Process p = null;
    boolean pythonWorkerExists = false;
    if (!pythonWorkerExists) {
      p = startPythonWorkerProcess();
    }

    if (pythonCompleteF.isDone() && !pythonCompleteF.get()) {
      log.error("Not submitting subjobs since python worker did not run");
      return false;
    } else {
      if (p != null)
        state.setPercent(2).setMessage("Started python worker");
      try {
        sendCommandsToPython();
      } catch (Exception e) {
        log.error("When sending command message to python worker", e);
        pythonCompleteF.complete(false);
      } finally {
        log.info("Waiting for python worker to finish");
        pythonCompleteF.get();
        producer.send(pythonDestQ, createEndPythonMsg());
      }

      if (p != null) {
        log.info("Waiting for subprocess thread to complete");
        p.waitFor(); // waits for subprocess to end
      }
    }

    return pythonCompleteF.get().booleanValue();
  }

  protected void sendCommandsToPython() throws Exception {
    producer.send(pythonDestQ, createCommandMsg());
  }

  private Process startPythonWorkerProcess() {
    // pWorkerThread = new Thread(() -> {
    Process p = null;
    try {
      // -u option so that output is unbuffered
      p = RuntimeUtils.exec(pythonExecFile, genPythonCmdAndArgs());
      Thread.sleep(1000); // allow some time for python to crash
      if (!p.isAlive()) {
        log.error("Python worker died");
        pythonCompleteF.complete(false);
      }
    } catch (Exception e) {
      log.error("When running python worker", e);
      pythonCompleteF.complete(false);
    }
    // }, "myPythonThread."+pythonExecFile);
    // pWorkerThread.start();
    // if (!pythonCompleteF.isDone())
    // Thread.sleep(2000); // allow some more time for python to crash before continuing
    return p;
  }

  private String[] genPythonCmdAndArgs() {
    return new String[] {pythonExecutable, "-u", pythonExecFile, pythonQueueName};
  }

  protected abstract Message createCommandMsg() throws JMSException;

  Message createEndPythonMsg() throws JMSException {
    log.info("createEndPythonMsg for python worker: {}", pythonExecFile);
    Message message = session.createTextMessage();
    message.setStringProperty("id", END);
    message.setStringProperty("command", END);
    return message;
  }
}
