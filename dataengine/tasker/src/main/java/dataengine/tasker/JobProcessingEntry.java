package dataengine.tasker;

import java.util.Properties;
import javax.jms.Connection;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dataengine.api.Progress;
import dataengine.api.State;
import dataengine.apis.DepJobService_I;
import dataengine.apis.ProgressState;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.RpcClientsModule;

@Slf4j
@RequiredArgsConstructor
class JobProcessingEntry {

  static interface Factory {
    JobProcessingEntry create(String amqAddress, 
        @Assisted("deliveryMode") int deliveryMode, 
        @Assisted("pollInterval") int progressPollIntervalSeconds);
  }

  final Connection connection;
  final RpcClientProvider<SessionsDB_I> sessDb;

  @Getter
  final JobListener_I jobListener;
  @Getter
  final String amqAddress;
  @Getter
  final RpcClientProvider<DepJobService_I> jobDispatcher;
  @Getter
  private int progressPollIntervalSeconds;

  @Inject
  public JobProcessingEntry(Connection connection,
      JobListener_I.Factory jobListenerFactory, RpcClientProvider<SessionsDB_I> sessDb,
      @Assisted String amqAddress, 
      @Assisted("deliveryMode") int deliveryMode, 
      @Assisted("pollInterval") int progressPollIntervalSeconds) {
    log.info("------ Creating {} at {}", this, amqAddress);
    this.connection = connection;
    this.sessDb = sessDb;
    this.amqAddress=amqAddress;
    this.progressPollIntervalSeconds = progressPollIntervalSeconds;
    if (progressPollIntervalSeconds < 0)
      this.progressPollIntervalSeconds = 2;
    jobDispatcher = new RpcClientProvider<>(
        RpcClientsModule.getAmqClientSupplierFor(connection, DepJobService_I.class, amqAddress, deliveryMode, true));
    this.jobListener = createJobListener(jobListenerFactory, jobDispatcher);
  }

  private JobListener_I createJobListenerForDispatcher(JobListener_I.Factory jobListenerFactory,
      Properties compProps, RpcClientProvider<DepJobService_I> jobDispatcher) {
    return jobListenerFactory.create(compProps, (msg, progressState) -> {
      updateSessionDb(sessDb, progressState);
      
      // notify jobDispatcher so that dependent jobs can be triggered
      State state = progressState.getState();
      if (state != null)
        switch (state) {
          case COMPLETED:
            jobDispatcher.rpc().handleJobCompleted(progressState.getJobId());
            break;
          case FAILED:
            jobDispatcher.rpc().handleJobFailed(progressState.getJobId());
            break;
          case CANCELLED:
            break;
          default:
            break;
        }
    });
  }

  private void updateSessionDb(RpcClientProvider<SessionsDB_I> sessDb,
      ProgressState progressState) {
    Progress progress =
        new Progress().percent(progressState.getPercent()).stats(progressState.getMetrics());
    log.info("updateJobProgress: {} {}", progressState.getJobId(), progress);
    // placeholder to do any checking
    sessDb.rpc().updateJobProgress(progressState.getJobId(), progress);

    State state = progressState.getState();
    log.info("updateJobState: {} {}", progressState.getJobId(), state);
    // placeholder to do any checking
    if (state != null)
      sessDb.rpc().updateJobState(progressState.getJobId(), state);
  }

  JobListener_I createJobListener(JobListener_I.Factory jobListenerFactory, 
      RpcClientProvider<DepJobService_I> depJobSvcRpc) {
    Properties compProps = new Properties();
    compProps.setProperty("_componentId", amqAddress+"-JobListener");
    return createJobListenerForDispatcher(jobListenerFactory, compProps, depJobSvcRpc);
  }


}
