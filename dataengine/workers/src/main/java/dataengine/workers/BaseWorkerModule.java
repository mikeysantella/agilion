package dataengine.workers;

import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.JobConsumer;
import net.deelam.vertx.jobboard.ProgressMonitor;
import net.deelam.vertx.jobboard.ProgressingDoer;
import net.deelam.vertx.jobboard.ReportingWorker;
import net.deelam.vertx.jobboard.VertxProgressMonitor;

@Slf4j
@RequiredArgsConstructor
public class BaseWorkerModule extends AbstractModule {
  private static final String NAMED_JOB_BOARD_ID = "jobBoardId";
  final String jobBoardId;

  @Override
  protected void configure() {
    bindConstant().annotatedWith(Names.named(NAMED_JOB_BOARD_ID)).to(jobBoardId);

    bind(ProgressMonitor.Factory.class).to(VertxProgressMonitor.Factory.class);
  }

  public static void deployDoers(Injector injector, final List<ProgressingDoer> doers) {
    final Vertx vertx = injector.getInstance(Vertx.class);
    String jobBoardId=injector.getInstance(Key.get(String.class, Names.named(NAMED_JOB_BOARD_ID)));
    doers.forEach(doer -> {
      ProgressMonitor.Factory pmFactory = injector.getInstance(ProgressMonitor.Factory.class);
      deployReportingWorker(vertx, pmFactory, jobBoardId, doer);
    });
  }

  public static void deployReportingWorker(Vertx vertx, ProgressMonitor.Factory pmFactory, 
      String jobBoardId, ProgressingDoer doer) {
    ReportingWorker rw = new ReportingWorker(doer, () -> doer.state())
        .setProgressMonitorFactory(pmFactory);

    JobConsumer jConsumer = new JobConsumer(jobBoardId, doer.jobType()).setWorker(rw);
    log.info("Deploying JobConsumer with ReportingWorker for: {}", doer.name());
    vertx.deployVerticle(jConsumer);
  }
}
