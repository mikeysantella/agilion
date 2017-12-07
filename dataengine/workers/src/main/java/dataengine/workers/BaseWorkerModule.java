package dataengine.workers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import com.google.inject.AbstractModule;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.JobBoardOutput_I;
import dataengine.apis.RpcClientProvider;
import dataengine.workers.ProgressMonitor.Factory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;

@Slf4j
@RequiredArgsConstructor
public class BaseWorkerModule extends AbstractModule {
  final List<ProgressingDoer> doers = new ArrayList<>();

  @Override
  protected void configure() {
    requireBinding(Connection.class);
    
    //bindConstant().annotatedWith(Names.named(NAMED_JOB_BOARD_ID)).to(jobBoardId);

    bind(ProgressMonitor.Factory.class).to(AmqProgressMonitor.Factory.class);
  }

  public static class DeployedJobConsumerFactory {
    final ProgressMonitor.Factory pmFactory;
    final RpcClientProvider<JobBoardOutput_I> jobBoard;
    final Connection connection;

    @Inject
    DeployedJobConsumerFactory(Factory pmFactory, RpcClientProvider<JobBoardOutput_I> jobBoard, Connection connection) {
      this.pmFactory = pmFactory;
      this.jobBoard=jobBoard;
      this.connection=connection;
    }

    public JobConsumer create(ProgressingDoer doer, String newJobAvailableTopic) {
      ReportingWorker rw = new ReportingWorker(doer, (job) -> doer.canDo(job), () -> doer.state())
          .setProgressMonitorFactory(pmFactory);
      
      JobConsumer jConsumer = new JobConsumer(doer.jobType(), jobBoard, connection).setWorker(rw);
      log.info("AMQ: WORKER: Deploying JobConsumer with ReportingWorker for: {} type={}", 
          doer.name(), doer.jobType());
      try {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        jConsumer.start(MQClient.createTopicConsumer(session, newJobAvailableTopic, null));
        return jConsumer;
      } catch (JMSException e) {
        throw new IllegalStateException("When creating newJobs topic listener", e);
      }
    }
  }
}
