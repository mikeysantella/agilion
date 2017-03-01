package dataengine.workers;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
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
  final String jobBoardId;
  final List<ProgressingDoer> doers = new ArrayList<>();

  private static final String NAMED_JOB_BOARD_ID = "jobBoardId";

  @Override
  protected void configure() {
    bindConstant().annotatedWith(Names.named(NAMED_JOB_BOARD_ID)).to(jobBoardId);

    bind(ProgressMonitor.Factory.class).to(VertxProgressMonitor.Factory.class);
  }

  @Provides
  public List<JobConsumer> deployDoers(Vertx vertx, ProgressMonitor.Factory pmFactory, @Named(NAMED_JOB_BOARD_ID) String jobBoardId) {
    return doers.stream().map(doer -> {
      //ProgressMonitor.Factory pmFactory = injector.getInstance(ProgressMonitor.Factory.class);
      return deployReportingWorker(vertx, pmFactory, jobBoardId, doer);
    }).collect(toList());
  }

  public static JobConsumer deployReportingWorker(Vertx vertx, ProgressMonitor.Factory pmFactory,
      String jobBoardId, ProgressingDoer doer) {
    ReportingWorker rw = new ReportingWorker(doer, () -> doer.state())
        .setProgressMonitorFactory(pmFactory);

    JobConsumer jConsumer = new JobConsumer(jobBoardId, doer.jobType()).setWorker(rw);
    log.info("Deploying JobConsumer with ReportingWorker for: {}", doer.name());
    vertx.deployVerticle(jConsumer);
    return jConsumer;
  }
}
