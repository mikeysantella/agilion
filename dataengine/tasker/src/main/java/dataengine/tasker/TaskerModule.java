package dataengine.tasker;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import dataengine.tasker.jobcreators.AddSourceDataset;
import io.vertx.core.Vertx;
import net.deelam.vertx.jobboard.DepJobService_I;
import net.deelam.vertx.rpc.RpcVerticleServer;

final class TaskerModule extends AbstractModule {
  @Override
  protected void configure() {
    requireBinding(Vertx.class);
    requireBinding(Key.get(new TypeLiteral<RpcClientProvider<SessionsDB_I>>() {}));
    requireBinding(Key.get(new TypeLiteral<RpcClientProvider<DepJobService_I>>() {}));

    // See http://stackoverflow.com/questions/14781471/guice-differences-between-singleton-class-and-singleton
    bind(JobListener_I.class).to(TaskerJobListener.class);
    bind(TaskerJobListener.class).in(Singleton.class);
    
    bind(Tasker_I.class).to(TaskerService.class);
    bind(TaskerService.class).in(Singleton.class);
  }
  
  @Provides
  List<JobsCreator> getJobCreators(Injector injector) {
    // TODO: 5: read jobCreators from file
    List<String> classes = new ArrayList<>();
    {
      classes.add(AddSourceDataset.class.getCanonicalName());
    };

    List<JobsCreator> jobCreators = classes.stream().map(jcClassName -> {
      try {
        @SuppressWarnings("unchecked")
        Class<? extends JobsCreator> addSrcDataClazz = (Class<? extends JobsCreator>) Class.forName(jcClassName);
        JobsCreator jc = injector.getInstance(addSrcDataClazz);
        return jc;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).collect(toList());
    return jobCreators;
  }

  static void deployTasker(Injector injector) {
    Vertx vertx = injector.getInstance(Vertx.class);
    TaskerService taskerSvc = injector.getInstance(TaskerService.class);
    new RpcVerticleServer(vertx, VerticleConsts.taskerBroadcastAddr)
        .start("TaskerServiceBusAddr" + System.currentTimeMillis(), taskerSvc, true);
  }
  
  static void deployJobListener(Injector injector) {
    Vertx vertx = injector.getInstance(Vertx.class);
    TaskerJobListener jobListener = injector.getInstance(TaskerJobListener.class);
    jobListener.setProgressPollIntervalSeconds(1); // TODO: 4: read from property file
    
    vertx.deployVerticle(jobListener);
  }
}
