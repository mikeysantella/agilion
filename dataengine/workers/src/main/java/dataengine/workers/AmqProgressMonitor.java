package dataengine.workers;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiFunction;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import dataengine.apis.ProgressState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.KryoSerDe;


@RequiredArgsConstructor
@Slf4j
@ToString
public class AmqProgressMonitor implements ProgressMonitor {

  @RequiredArgsConstructor
  public static class Factory implements ProgressMonitor.Factory {
    private final Connection connection;
    private final int deliveryMode;

    public ProgressMonitor create(String jobId, int pollIntervalInSeconds, String busAddr) {
      checkNotNull(busAddr);
      return new AmqProgressMonitor(connection, deliveryMode, jobId, pollIntervalInSeconds, busAddr);
    }
  }

  private final Connection connection;
  private final int deliveryMode;
  private final String jobId;
  private final int pollIntervalInSeconds;
  private final String busAddr;

  private HasProgress progressMaker = null;
  private Timer timer;

  @Override
  public void setProgressMaker(HasProgress process) {
    if (progressMaker != null)
      throw new IllegalStateException("Cannot set another progressMaker!");
    progressMaker = process;

    getSession(); // initializes session

    if (pollIntervalInSeconds > 0) {
      timer = new Timer();
      timer.schedule(new TimerTask() {
        public void run() {
          doUpdate();
          if (isClosed && !isStopped)
            log.warn("Progress monitor has closed but progressMaker is not complete! {}",
                progressMaker);
        }
      }, 10, // initial delay
          pollIntervalInSeconds * 1000); // subsequent rate
    } else {
      log.warn("Polling timer not set: pollIntervalInSeconds={}", pollIntervalInSeconds);
    }
  }

  private boolean isClosed = false;

  @Override
  public void close() {
    if (progressMaker == null) {
      if (!isStopped) { // then not DONE or FAILED
        log.warn("Assuming progressMaker is done, sending 100%: {}", this);
        update(new ProgressState(100, "Assuming done: " + jobId));
      }
    } else if (!isStopped) {
      doUpdate(); // will call stop() if done or failed; otherwise, timer will continue to monitor
                  // progressMaker
      isClosed = true;

      if (!isStopped)
        log.warn("Progress monitor has closed but progressMaker is not complete! {}",
            progressMaker);
    }
  }

  boolean isStopped = false;

  @Override
  public void stop() {
    if (isStopped) {
      log.debug("stop() already called!");
      return;
    }
    isStopped = true;

    if (timer != null)
      timer.cancel();
    timer = null;

    if (progressMaker != null) {
      ProgressState p = progressMaker.getProgress();
      if (p.getPercent() > 0 && p.getPercent() < 100)
        log.warn("Stopping progress updates for {} before completion: {}", jobId, p);
      update(p);
      progressMaker = null;
    }
  }

  private void doUpdate() {
    if (progressMaker == null) {
      log.warn("Cannot doUpdate without progressMaker={}", this); // should not occur
      // manually done via update(ProgressState): update(new ProgressState(MIN_PROGRESS, "Activity
      // initialized but has not made progress: " + requestId));
    } else {
      ProgressState p = progressMaker.getProgress();
      log.debug("Progress of {} by {}: {}", jobId, progressMaker, p);

      // accumulate metrics in props
      // p.getMetrics().entrySet().stream().forEach(e -> props.put(e.getKey(), e.getValue()));
      update(p);
    }
  }

  private BiFunction<AmqProgressMonitor, ProgressState, BytesMessage> messageProvider;

  @Override
  public void update(ProgressState state) {
    { // sanity check
      checkAgainstLastPercent(state);
    }

    if (state.getPercent() < 0 || state.getPercent() >= 100) {
      stop();
    }
    try {
      BytesMessage message = createJobStatusMsg(state);
      if (message != null)
        producer.send(message);
    } catch (JMSException e) {
      log.error("When broadcasting job status msg", e);
    }
  }

  private BytesMessage createJobStatusMsg(ProgressState state) {
    try {
      BytesMessage msg = serde.writeObject(state);
      msg.setStringProperty(ProgressState.JOBID_KEY, state.getJobId());
      return msg;
    } catch (JMSException e) {
      log.error("When serializing job state", e);
      return null;
    }
  }

  MessageProducer producer;
  KryoSerDe serde;

  @Getter(lazy = true)
  private final Session session = privateCreateSession();
  private Session privateCreateSession() {
    try {
      Session sess = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      serde = new KryoSerDe(sess);
      producer = MQClient.createTopicMsgSender(sess, busAddr, deliveryMode);
      return sess;
    } catch (JMSException e) {
      throw new IllegalStateException("When initializing session", e);
    }
  }

  int __sanity_lastPercentSent = 0;

  private void checkAgainstLastPercent(ProgressState state) {
    // log.debug("Sending: {}", progressMsg);
    int percent = state.getPercent();
    if (percent > 0 && percent < __sanity_lastPercentSent) {
      log.warn("Not expecting to send {} < {}", state.getPercent(), __sanity_lastPercentSent);
    }
    if (percent > 100)
      log.warn("Not expecting >100: {}", percent);
    __sanity_lastPercentSent = percent;
  }

}
