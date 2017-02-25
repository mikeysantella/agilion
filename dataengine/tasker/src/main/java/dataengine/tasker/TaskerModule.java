package dataengine.tasker;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import net.deelam.vertx.rpc.RpcVerticleServer;

final class TaskerModule extends AbstractModule {
  @Override
  protected void configure() {
    // See http://stackoverflow.com/questions/14781471/guice-differences-between-singleton-class-and-singleton

    bind(Tasker_I.class).to(TaskerService.class);
    bind(TaskerService.class).in(Singleton.class);
  }

  static void deployTasker(Injector injector) {
    Vertx vertx = injector.getInstance(Vertx.class);
    
    TaskerService taskerSvc = injector.getInstance(TaskerService.class);
    new RpcVerticleServer(vertx, VerticleConsts.taskerBroadcastAddr)
        .start("TaskerServiceBusAddr", taskerSvc);

    new RpcVerticleServer(vertx, VerticleConsts.jobListenerBroadcastAddr)
        .start("JobListenerBusAddr", taskerSvc);
  }
}
