package dataengine.workers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.AmqProgressMonitor;
import net.deelam.vertx.jobboard.JobBoardOutput_I;
import net.deelam.vertx.jobboard.JobConsumer;
import net.deelam.vertx.jobboard.ProgressMonitor;
import net.deelam.vertx.jobboard.ProgressMonitor.Factory;
import net.deelam.vertx.jobboard.ProgressingDoer;
import net.deelam.vertx.jobboard.ReportingWorker;

@Slf4j
@RequiredArgsConstructor
public class BaseWorkerModule extends AbstractModule {
  final String jobBoardId;
  final List<ProgressingDoer> doers = new ArrayList<>();

  private static final String NAMED_JOB_BOARD_ID = "jobBoardId";

  @Override
  protected void configure() {
    requireBinding(Vertx.class);
    requireBinding(Connection.class);
    
    checkNotNull(jobBoardId);
    checkArgument(jobBoardId.length() > 0);
    log.info("jobBoardId={}", jobBoardId);
    bindConstant().annotatedWith(Names.named(NAMED_JOB_BOARD_ID)).to(jobBoardId);

    //bind(ProgressMonitor.Factory.class).to(VertxProgressMonitor.Factory.class);
    bind(ProgressMonitor.Factory.class).to(AmqProgressMonitor.Factory.class);
  }

  public static class DeployedJobConsumerFactory {
    final Vertx vertx;
    final ProgressMonitor.Factory pmFactory;
    final String jobBoardId;
    final RpcClientProvider<JobBoardOutput_I> jobBoard;
    final Connection connection;

    @Inject
    DeployedJobConsumerFactory(Vertx vertx, Factory pmFactory, @Named(NAMED_JOB_BOARD_ID) String jobBoardId, RpcClientProvider<JobBoardOutput_I> jobBoard, Connection connection) {
      this.vertx = vertx;
      this.pmFactory = pmFactory;
      this.jobBoardId = jobBoardId;
      this.jobBoard=jobBoard;
      this.connection=connection;
    }

    public JobConsumer create(ProgressingDoer doer) {
      checkNotNull(jobBoardId);
      checkArgument(jobBoardId.length() > 0);
      
      ReportingWorker rw = new ReportingWorker(doer, (job) -> doer.canDo(job), () -> doer.state())
          .setProgressMonitorFactory(pmFactory);
      
      JobConsumer jConsumer = new JobConsumer(jobBoardId, doer.jobType(), jobBoard, connection).setWorker(rw);
      log.info("AMQ: WORKER: Deploying JobConsumer jobBoardId={} with ReportingWorker for: {} type={}", 
          jobBoardId, doer.name(), doer.jobType());
      jConsumer.start(createTopicConsumer(connection, VerticleConsts.newJobAvailableTopic));
      //vertx.deployVerticle(jConsumer);
      return jConsumer;
    }
    
    static MessageConsumer createTopicConsumer(Connection connection, String newJobTopic){
      try {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination queue = session.createTopic(newJobTopic);
        return session.createConsumer(queue);
      } catch (JMSException e) {
        throw new IllegalStateException("When setting up topic listener", e);
      }
    }

  }
}
