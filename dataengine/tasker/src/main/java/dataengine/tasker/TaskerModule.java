package dataengine.tasker;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

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
  
  @Provides
  List<JobsCreator> getJobCreators() {
    // TODO: 5: read jobCreators from file
    List<String> classes = new ArrayList<>();
    {
      classes.add(AddSourceDataset.class.getCanonicalName());
    };

    List<JobsCreator> jobCreators = classes.stream().map(jcClassName -> {
      try {
        @SuppressWarnings("unchecked")
        Class<? extends JobsCreator> addSrcDataClazz = (Class<? extends JobsCreator>) Class.forName(jcClassName);
        JobsCreator jc = addSrcDataClazz.getConstructor().newInstance();
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

    new RpcVerticleServer(vertx, VerticleConsts.jobListenerBroadcastAddr)
        .start("JobListenerBusAddr" + System.currentTimeMillis(), taskerSvc, true);
  }
}
