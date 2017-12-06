package dataengine.jobmgr;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.ActiveMqRpcServer;
import net.deelam.graph.GrafUri;
import net.deelam.graph.IdGrafFactoryNeo4j;
import net.deelam.graph.IdGrafFactoryTinker;
import net.deelam.vertx.jobboard.DepJobFrame;
import net.deelam.vertx.jobboard.DepJobService;
import net.deelam.vertx.jobboard.JobBoard;
import net.deelam.vertx.jobboard.JobProducer;
import net.deelam.vertx.rpc.RpcVerticleServer;

@RequiredArgsConstructor
@Slf4j
public class JobBoardModule extends AbstractModule {
  final String jobBoardId;
  
  @Override
  protected void configure() {
    requireBinding(Vertx.class);
    
    JobProducer jobProducerProxy = new JobProducer(jobBoardId);
    bind(JobProducer.class).toInstance(jobProducerProxy);

    JobBoard jm = new JobBoard(jobBoardId, jobBoardId+System.currentTimeMillis());
    bind(JobBoard.class).toInstance(jm);
  }

  static void deployJobBoardVerticles(Injector injector) {
    Vertx vertx = injector.getInstance(Vertx.class);
    JobProducer jobProducer = injector.getInstance(JobProducer.class);
    JobBoard jobBoard = injector.getInstance(JobBoard.class);
    if(DEBUG){
      jobBoard.periodicLogs(10_000, 20);
    }
    
    log.info("VERTX: TASKER: Deploying JobBoard: {} ", jobBoard); 
    vertx.deployVerticle(jobBoard);
    log.info("VERTX: TASKER: Deploying JobProducer for jobBoardId={}", jobProducer.getServiceType()); 
    vertx.deployVerticle(jobProducer);
  }

  static void deployDepJobService(Injector injector, String depJobMgrId) {
    Vertx vertx = injector.getInstance(Vertx.class);
    JobProducer jobProducer = injector.getInstance(JobProducer.class);
    GrafUri depJobGrafUri;
    
    if(false){
      IdGrafFactoryNeo4j.register();
      depJobGrafUri = new GrafUri("neo4j:jobMgrDB");
    } else {
      IdGrafFactoryTinker.register();
      depJobGrafUri = new GrafUri("tinker:/");
    }
    IdGraph<?> depJobMgrGraf = depJobGrafUri.openIdGraph();

    // requires that jobProducerProxy be deployed
    DepJobService depJobMgr = new DepJobService(depJobMgrGraf, ()->jobProducer);
    log.info("AMQ: TASKER: Deploying RPC service for DepJobService: {}", depJobMgr); 
//    new RpcVerticleServer(vertx, depJobMgrId)
//      .start(depJobMgrId+System.currentTimeMillis(), depJobMgr, true);
    injector.getInstance(ActiveMqRpcServer.class).start(depJobMgrId, depJobMgr, true);
    
    if(DEBUG){
      vertx.setPeriodic(10_000, t -> {
        if (depJobMgr.getWaitingJobs().size() > 0)
          log.info("waitingJobs={}", depJobMgr.getWaitingJobs().keySet());
        if (depJobMgr.getUnsubmittedJobs().size() > 0)
          log.info("unsubmittedJobs={}", depJobMgr.getUnsubmittedJobs().keySet());
      });
      
      int statusPeriod = 10_000;
      int sameLogThreshold = 10;
      if (statusPeriod > 0) {
        AtomicInteger sameLogMsgCount = new AtomicInteger(0);
        vertx.setPeriodic(statusPeriod, id -> {
          String logMsg = depJobMgr.toStringRemainingJobs(DepJobFrame.STATE_PROPKEY);
          if (!logMsg.equals(depJobMgrPrevLogMsg)) {
            log.info(logMsg);
            depJobMgrPrevLogMsg = logMsg;
            sameLogMsgCount.set(0);
          } else {
            if (sameLogMsgCount.incrementAndGet() > sameLogThreshold)
              depJobMgrPrevLogMsg = null;
          }
        });
      }
    }
  }
  
  private static final boolean DEBUG = true;
  private static String depJobMgrPrevLogMsg;

}

