package dataengine.workers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.JobConsumer;
import net.deelam.vertx.jobboard.ProgressMonitor;
import net.deelam.vertx.jobboard.ProgressMonitor.Factory;
import net.deelam.vertx.jobboard.ProgressingDoer;
import net.deelam.vertx.jobboard.ReportingWorker;
import net.deelam.vertx.jobboard.VertxProgressMonitor;

@Slf4j
@RequiredArgsConstructor
public class BaseWorkerModule extends AbstractModule {
  final String jobBoardId;
  final List<ProgressingDoer> doers = new ArrayList<>();

  private static final String NAMED_JOB_BOARD_ID = "jobBoardId";

  @Override
  protected void configure() {
    requireBinding(Vertx.class);
    
    checkNotNull(jobBoardId);
    checkArgument(jobBoardId.length() > 0);
    log.info("jobBoardId={}", jobBoardId);
    bindConstant().annotatedWith(Names.named(NAMED_JOB_BOARD_ID)).to(jobBoardId);

    bind(ProgressMonitor.Factory.class).to(VertxProgressMonitor.Factory.class);
  }

  public static class DeployedJobConsumerFactory {
    final Vertx vertx;
    final ProgressMonitor.Factory pmFactory;
    final String jobBoardId;

    @Inject
    DeployedJobConsumerFactory(Vertx vertx, Factory pmFactory, @Named(NAMED_JOB_BOARD_ID) String jobBoardId) {
      this.vertx = vertx;
      this.pmFactory = pmFactory;
      this.jobBoardId = jobBoardId;
    }

    public JobConsumer create(ProgressingDoer doer) {
      checkNotNull(jobBoardId);
      checkArgument(jobBoardId.length() > 0);
      
      ReportingWorker rw = new ReportingWorker(doer, (job) -> doer.canDo(job), () -> doer.state())
          .setProgressMonitorFactory(pmFactory);
      
      JobConsumer jConsumer = new JobConsumer(jobBoardId, doer.jobType()).setWorker(rw);
      log.info("VERTX: WORKER: Deploying JobConsumer jobBoardId={} with ReportingWorker for: {} type={}", 
          jobBoardId, doer.name(), doer.jobType());
      vertx.deployVerticle(jConsumer);
      return jConsumer;
    }

  }
}
