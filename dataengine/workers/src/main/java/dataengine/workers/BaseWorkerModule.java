package dataengine.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.JobBoardOutput_I;
import dataengine.apis.RpcClientProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.AmqComponentSubscriber;

@Slf4j
@RequiredArgsConstructor
public class BaseWorkerModule extends AbstractModule {
  final Properties configMap;
  final int deliveryMode;
  final List<ProgressingDoer> doers = new ArrayList<>();
  
  @Override
  protected void configure() {
    requireBinding(Connection.class);
    
    //bindConstant().annotatedWith(Names.named(NAMED_JOB_BOARD_ID)).to(jobBoardId);
    bind(Properties.class).toInstance(configMap);
  }
  
  @Provides
  ProgressMonitor.Factory createProgressMonitorFactory(Connection connection){
    return new AmqProgressMonitor.Factory(connection, deliveryMode);
  }

  public static class DeployedJobConsumerFactory {
    final ProgressMonitor.Factory pmFactory;
    final RpcClientProvider<JobBoardOutput_I> jobBoardRpc;
    final Connection connection;
    
    @Inject
    DeployedJobConsumerFactory(ProgressMonitor.Factory pmFactory, RpcClientProvider<JobBoardOutput_I> jobBoard, Connection connection) {
      this.pmFactory = pmFactory;
      this.jobBoardRpc = jobBoard;
      this.connection = connection;
    }

    public JobConsumer create(ProgressingDoer doer, String newJobAvailableTopic, int deliveryMode) {
      ReportingWorker rw = new ReportingWorker(doer, doer::canDo, doer::state)
          .setProgressMonitorFactory(pmFactory);
      
      JobConsumer jConsumer = new JobConsumer(doer.jobType(), jobBoardRpc, connection, rw);
      log.info("AMQ: WORKER: Deploying JobConsumer with ReportingWorker for: {} type={}", 
          doer.name(), doer.jobType());
      try {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        String workerAddr=jConsumer.start("JobConsumer-"+doer.name(), 
            MQClient.createTopicConsumer(session, newJobAvailableTopic, null));

        new AmqComponentSubscriber(connection, deliveryMode, workerAddr, 
            CommunicationConsts.COMPONENT_TYPE, "JobConsumer", 
            "jobType", doer.jobType());
        return jConsumer;
      } catch (JMSException e) {
        throw new IllegalStateException("When creating newJobs topic listener", e);
      }
    }
  }
}
