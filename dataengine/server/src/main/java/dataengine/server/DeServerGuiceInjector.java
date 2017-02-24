package dataengine.server;

import java.util.concurrent.CompletableFuture;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.api.DatasetApiService;
import dataengine.api.JobApiService;
import dataengine.api.OperationsApiService;
import dataengine.api.RequestApiService;
import dataengine.api.SessionApiService;
import dataengine.api.SessionsApiService;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Accessors(fluent = true)
@Slf4j
public final class DeServerGuiceInjector {

  @Getter
  static final Injector singleton = new DeServerGuiceInjector().injector();

  @Getter
  final Injector injector;

  private DeServerGuiceInjector() {
    CompletableFuture<Vertx> vertxF = new CompletableFuture<>();
    ClusteredVertxConfig vertxConfig=new ClusteredVertxConfig();
    injector = Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF, vertxConfig),
        new VertxRpcClients4ServerModule(vertxF),
        new RestServiceModule());

    String runAllInSameJvm = System.getProperty("RUN_ALL_IN_SAME_JVM");
    if(Boolean.valueOf(runAllInSameJvm)){
      log.info("======== Running all required DataEngine services in same JVM");
      startAllInSameJvm();
    }
    log.info("Created DeServerGuiceInjector");
  }

  private void startAllInSameJvm() {
    String[] args=null;
    dataengine.sessions.SessionsMain.main(args);
    dataengine.tasker.TaskerMain.main(args);
    dataengine.jobmgr.JobManagerMain.main(args);
    dataengine.workers.WorkerMain.main(args);
  }

  static class RestServiceModule extends AbstractModule {
    @Override
    protected void configure() {
      log.info("Binding services for REST");
      /// bind REST services
      bind(SessionsApiService.class).to(MySessionsApiService.class);
      bind(SessionApiService.class).to(MySessionApiService.class);
      bind(DatasetApiService.class).to(MyDatasetApiService.class);
      bind(JobApiService.class).to(MyJobApiService.class);
      bind(RequestApiService.class).to(MyRequestApiService.class);
      bind(OperationsApiService.class).to(MyOperationsApiService.class);
    }
  }
}
