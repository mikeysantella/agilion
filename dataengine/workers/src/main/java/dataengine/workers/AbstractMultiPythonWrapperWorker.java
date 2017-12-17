package dataengine.workers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMultiPythonWrapperWorker extends AbstractPythonWrapperWorker {

  @Inject
  public AbstractMultiPythonWrapperWorker(RpcClientProvider<SessionsDB_I> sessDb, Connection connection,
      String jobType, String pythonExecFile)
      throws JMSException {
    super(sessDb, connection, jobType, pythonExecFile);
  }

  @RequiredArgsConstructor
  static class SubjobProgress {
    final int portion;
    int percent = 0;
    CompletableFuture<Boolean> pythonCompleteF = new CompletableFuture<>();
  }

  protected Map<String, SubjobProgress> subjobsProgress = new HashMap<>();

  @Override
  protected void onPythonReply(Message m, int percent, String message) throws JMSException {
    // update subjob's progress
    String subJobId = m.getStringProperty("id");
    SubjobProgress subjobProgress = subjobsProgress.get(subJobId);
    subjobProgress.percent = percent;
    AbstractPythonWrapperWorker.updateFuture(percent, subjobProgress.pythonCompleteF);

    // calculate parent job's progress
    int percentSum =
        subjobsProgress.values().stream().mapToInt(sjP -> Math.abs(sjP.percent) * sjP.portion / 100).sum();
    boolean anyFailed=subjobsProgress.values().stream().filter(sjP -> sjP.percent<0).findAny().isPresent();
    if(anyFailed)
      percentSum*=-1;
    log.info("job percent={}", percentSum);
    state.setPercent(percentSum).setMessage(message);
    AbstractPythonWrapperWorker.updateFuture(percentSum, pythonCompleteF);
  }

  @Override
  protected void sendCommandsToPython() throws Exception {
    subjobsProgress.clear();
    int i=0;
    SubjobCommandMsg cmdMsg;
    int totalPercentAllocated=0;
    while((cmdMsg=createCommandMsg(i++)) != null) {
      String subjobId = cmdMsg.msg.getStringProperty("id");
      subjobsProgress.put(subjobId, cmdMsg.subjobProgress);
      totalPercentAllocated+=cmdMsg.subjobProgress.portion;
      producer.send(pythonDestQ, cmdMsg.msg);
      if(!cmdMsg.subjobProgress.pythonCompleteF.get()) {
        log.error("Subjob '{}' failed; not submitting subsequent subjobs", subjobId);
        throw new IllegalStateException("Failed subjob: "+subjobId);
      }
    }
    if(totalPercentAllocated!=100)
      throw new IllegalStateException("Subjob portions total was "+totalPercentAllocated);
  }

  @AllArgsConstructor
  //@Accessors(chain=true)
  protected static class SubjobCommandMsg {
    final SubjobProgress subjobProgress;
    final Message msg;
  }
  
  protected abstract SubjobCommandMsg createCommandMsg(int cmdIndex) throws JMSException;
  
  @Override
  protected final Message createCommandMsg() throws JMSException {
    throw new IllegalStateException("Should never be called");
  }
  
 }
