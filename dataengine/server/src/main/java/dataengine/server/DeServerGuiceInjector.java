package dataengine.server;

import java.util.concurrent.CompletableFuture;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import dataengine.api.DatasetApiService;
import dataengine.api.JobApiService;
import dataengine.api.OperationsApiService;
import dataengine.api.RequestApiService;
import dataengine.api.SessionApiService;
import dataengine.api.SessionsApiService;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxInjectionModule;
import net.deelam.vertx.jobboard.DepJobService_I;

@Accessors(fluent = true)
@Slf4j
public final class DeServerGuiceInjector {

  // complete this vertxF before calling singleton() to provide a vertx
  // otherwise, this will create its own vertx instance
  @Getter
  static CompletableFuture<Vertx> vertxF = new CompletableFuture<>();

  public static Injector singleton() {
    return new DeServerGuiceInjector().injector();
  }

  @Getter
  final Injector injector;

  private DeServerGuiceInjector() {
    injector = Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF),
        new VertxRpcClients4ServerModule(vertxF),
        new RestServiceModule());
    log.info("Created DeServerGuiceInjector");
  }

  static class RestServiceModule extends AbstractModule {
    @Override
    protected void configure() {
      requireBinding(Vertx.class);
      requireBinding(Key.get(new TypeLiteral<RpcClientProvider<SessionsDB_I>>() {}));
      requireBinding(Key.get(new TypeLiteral<RpcClientProvider<DepJobService_I>>() {}));
      requireBinding(Key.get(new TypeLiteral<RpcClientProvider<Tasker_I>>() {}));
      requireBinding(Key.get(new TypeLiteral<RpcClientProvider<OperationsRegistry_I>>() {}));

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
