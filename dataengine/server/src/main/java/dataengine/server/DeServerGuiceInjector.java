package dataengine.server;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.jms.Connection;
import javax.jms.JMSException;
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
import dataengine.apis.DepJobService_I;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.utils.PropertiesUtil;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Accessors(fluent = true)
@Slf4j
public final class DeServerGuiceInjector {

  // complete this vertxF before calling singleton() to provide a vertx
  // otherwise, this will create its own vertx instance
  @Getter
  static CompletableFuture<Vertx> vertxF = new CompletableFuture<>();

  static Injector singleton;
  public static Injector singleton() {
    if(singleton==null) {
      log.info("Starting {}", DeServerGuiceInjector.class);
      String brokerUrl = brokerUrl4Java();
      singleton=new DeServerGuiceInjector(brokerUrl).injector();
    }
    return singleton;
  }

  // load brokerUrl from file; return first url starting with "tcp:"
  public static String brokerUrl4Java() {
    String brokerUrl=properties().getProperty("brokerUrl"); // "tcp://localhost:33333,stomp://localhost:45679";
    checkNotNull(brokerUrl);
    for(String url:brokerUrl.split(","))
      if(url.toLowerCase().startsWith("tcp://"))
          return url;
    throw new IllegalArgumentException("Could not identify URL starting with 'tcp://'");
  }

  public static Properties properties() {
    Properties properties=new Properties();
    try {
      PropertiesUtil.loadProperties("bootstrap.props", properties);
    } catch (IOException e) {
      throw new IllegalStateException("When reading bootstrap.props", e);
    }
    properties.forEach((k,v)->log.debug("  "+k+"="+v));
    return properties;
  }

  static {
    Map<Class<?>, Integer> classToIntMap = new HashMap<>();
    //VertxRpcUtil.KryoSerDe.classRegis=classToIntMap;
    
    Class<?>[] classes={
      dataengine.api.Dataset.class,
      dataengine.api.Job.class,
      dataengine.api.Operation.class,
      dataengine.api.OperationMap.class,
      dataengine.api.OperationParam.class,
      dataengine.api.OperationSelection.class,
      dataengine.api.OperationSelectionMap.class,
      dataengine.api.Progress.class,
      dataengine.api.Request.class,
      dataengine.api.Session.class,
      dataengine.api.State.class,
      java.net.URI.class,
      java.util.ArrayList.class,
      java.util.HashMap.class,
      java.util.LinkedHashMap.class,
      dataengine.apis.JobDTO.class,
      Object[].class,
      String[].class,
      java.time.OffsetDateTime.class,
      dataengine.api.OperationParam.ValuetypeEnum.class,
    };

    for(int i=0; i<classes.length; ++i)
      classToIntMap.put(classes[i], 100+i);
    
  }
  
  @Getter
  final Injector injector;

  private DeServerGuiceInjector(String brokerUrl) {
    try {
      Connection connection = MQClient.connect(brokerUrl);
      injector = Guice.createInjector(
          new ClusteredVertxInjectionModule(vertxF),
          new VertxRpcClients4ServerModule(vertxF, connection),
          new RestServiceModule());
      connection.start();
      log.info("Created DeServerGuiceInjector");
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
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
