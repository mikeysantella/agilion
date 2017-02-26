package dataengine.tasker;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import dataengine.api.Operation;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import dataengine.tasker.jobcreators.AddSourceDataset;
import io.vertx.core.Vertx;
import net.deelam.vertx.rpc.RpcVerticleServer;

final class TaskerModule extends AbstractModule {
  @Override
  protected void configure() {
    // See http://stackoverflow.com/questions/14781471/guice-differences-between-singleton-class-and-singleton

    bind(Tasker_I.class).to(TaskerService.class);
    bind(TaskerService.class).in(Singleton.class);
  }

  static List<JobsCreator> getJobCreators(Map<String, Operation> currOperations) {
    List<JobsCreator> jobCreators = Lists.newArrayList( // TODO: 5: read jobCreators from file
        new AddSourceDataset(currOperations));
    return jobCreators;
  }

  static void deployTasker(Injector injector) {
    Vertx vertx = injector.getInstance(Vertx.class);
    
    TaskerService taskerSvc = injector.getInstance(TaskerService.class);
    new RpcVerticleServer(vertx, VerticleConsts.taskerBroadcastAddr)
        .start("TaskerServiceBusAddr"+System.currentTimeMillis(), taskerSvc, true);

    new RpcVerticleServer(vertx, VerticleConsts.jobListenerBroadcastAddr)
        .start("JobListenerBusAddr"+System.currentTimeMillis(), taskerSvc, true);
  }
}
