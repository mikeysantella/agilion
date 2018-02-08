package dataengine.server;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.ConstantsAmq;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.AmqComponentSubscriber;
import net.deelam.utils.PropertiesUtil;

@Accessors(fluent = true)
@Slf4j
public final class DeServerGuiceInjector {

  // complete this vertxF before calling singleton() to provide a vertx
  // otherwise, this will create its own vertx instance
//  @Getter
//  static CompletableFuture<Vertx> vertxF = new CompletableFuture<>();

  static DeServerGuiceInjector deServerGuiceInjector;
  static Injector singleton;
  public static Injector singleton() {
    if(singleton==null) {
      log.info("Starting {}", DeServerGuiceInjector.class);
      String brokerUrl = brokerUrl4Java();
      deServerGuiceInjector = new DeServerGuiceInjector(brokerUrl);
      singleton=deServerGuiceInjector.injector();
    }
    return singleton;
  }
  public static void shutdownSingleton() {
    if(deServerGuiceInjector!=null)
      deServerGuiceInjector.shutdown();
  }

  // load brokerUrl from file; return first url starting with "tcp:"
  public static String brokerUrl4Java() {
    String brokerUrlStr = System.getProperty(ConstantsAmq.BROKER_URL);
    if (brokerUrlStr == null || brokerUrlStr.length()==0)
      brokerUrlStr=properties().getProperty("brokerUrl");
    checkNotNull(brokerUrlStr);
    return ConstantsAmq.getTcpBrokerUrl(brokerUrlStr);
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
  private final Connection connection;
  private DeServerGuiceInjector(String brokerUrl) {
    try {
      connection = MQClient.connect(brokerUrl);
      injector = Guice.createInjector(
          new RpcClients4ServerModule(connection),
          new RestServiceModule());
      log.info("Created DeServerGuiceInjector");
      new AmqComponentSubscriber(connection, "RESTService");
    } catch (JMSException e) {
      throw new IllegalArgumentException(e);
    }
  }
  public void shutdown() {
    try {
      connection.close();
    } catch (JMSException e) {
      throw new IllegalStateException(e);
    }
  }

  static class RestServiceModule extends AbstractModule {
    @Override
    protected void configure() {
      requireBinding(Key.get(new TypeLiteral<RpcClientProvider<SessionsDB_I>>() {}));
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
