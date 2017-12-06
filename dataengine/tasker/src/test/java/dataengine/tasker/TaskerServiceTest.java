package dataengine.tasker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.jms.Connection;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.apis.DepJobService_I;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.MQService;

@Slf4j
public class TaskerServiceTest {
  
  BrokerService broker;
  
  @After
  public void shutdown() {
    try {
      broker.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Before
  public void setUp() throws Exception {
    CompletableFuture<Vertx> vertxF = CompletableFuture.completedFuture(Vertx.vertx());
    Injector injector = Guice.createInjector(
        new TaskerModule(new Properties()),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Vertx.class).toInstance(vertxF.join());

            // OperationsRegistryVerticle to which operations are registered by providers (ie, Workers)
            if(false) {
            } else {
              try {
                String brokerURL="tcp://localhost:55555";
                broker = MQService.createBrokerService("test", brokerURL);
                // OperationsRegistry to which operations are registered by providers (ie, Workers)
                Connection connection=MQClient.connect(brokerURL);
                bind(Connection.class).toInstance(connection);
                connection.start();
                OperationsRegistry opsReg =
                    new OperationsRegistry(connection, VerticleConsts.opsRegBroadcastAddr);
                bind(OperationsRegistry.class).toInstance(opsReg);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
          }

          @Provides
          RpcClientProvider<DepJobService_I> jobDispatcher_RpcClient() {
            return new RpcClientProvider<>(() -> Mockito.mock(DepJobService_I.class));
          }

          @Provides
          RpcClientProvider<SessionsDB_I> sessionsDb_RpcClient() {
            return new RpcClientProvider<>(() -> sessDB);
          }

        });
    opsReg = injector.getInstance(OperationsRegistry.class);
    {
      Map<String, String> info = new HashMap<>();
      info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
      Operation worker1Op = new Operation()
          .id("INGEST_SOURCE_DATASET")
          .description("add source dataset")
          .info(info)
          .addParamsItem(new OperationParam()
              .key("inputUri").required(true)
              .description("location of source dataset")
              .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
              .defaultValue(null))
          .addParamsItem(new OperationParam()
              .key(OperationConsts.DATA_FORMAT).required(true)
              .description("type and format of data")
              .valuetype(ValuetypeEnum.ENUM).isMultivalued(false)
              .defaultValue(null)
              .addPossibleValuesItem("TELEPHONE.CSV"));
      opsReg.getOperations().put(worker1Op.getId(), worker1Op);
    }

    taskerSvc = injector.getInstance(TaskerService.class);
    taskerSvc.refreshJobsCreators().join();
  }

  SessionsDB_I sessDB = mock(SessionsDB_I.class);
  OperationsRegistry opsReg;
  Tasker_I taskerSvc;

  //@Test
  public void testQueryingWorkers() throws InterruptedException, ExecutionException {
    Map<String, Operation> ops1 = opsReg.getOperations();
    log.info("before refresh: ops1={}", ops1);

    // simulate REST API call to OperationsApiService.refresh()
    opsReg.refresh()
        .thenCompose((none) -> taskerSvc.refreshJobsCreators())
        .join();

    Map<String, Operation> ops2 = opsReg.getOperations();
    log.info("after refresh: ops2={}", ops2);
  }

  @Test
  public void testSubmitIncompleteRequest() throws InterruptedException, ExecutionException {
    Map<String, Operation> ops = opsReg.getOperations();
    log.info("ops={}", ops);
    try {
      Request req = new Request().sessionId("newSess").label("req1Name")
          .operation(new OperationSelection().id("AddSourceDataset"));
      when(sessDB.addRequest(req)).thenReturn(CompletableFuture.completedFuture(req));
      taskerSvc.submitRequest(req).get();
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("parameters missing"));
    }
  }

  @Test
  public void testSubmitCompleteRequestButWrongEnum() throws InterruptedException, ExecutionException {
    try {
      HashMap<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("inputUri", "hdfs://some/where/");
      paramValues.put("dataFormat", "SOME_UNKNOWN_FORMAT");
      paramValues.put("ingesterWorker", "IngestTelephoneDummyWorker");
      Request req = new Request().sessionId("newSess").label("req1Name")
          .operation(new OperationSelection().id("AddSourceDataset").params(paramValues));
      when(sessDB.addRequest(req)).thenReturn(CompletableFuture.completedFuture(req));
      taskerSvc.submitRequest(req).get();
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Unknown enum"));
    }
  }

  @Test
  public void testSubmitCompleteRequestButWrongValueType() throws InterruptedException, ExecutionException {
    try {
      HashMap<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("inputUri", "hdfs://some/where/");
      paramValues.put("dataFormat", 123);
      paramValues.put("ingesterWorker", "IngestTelephoneDummyWorker");
      
      Request req = new Request().sessionId("newSess").label("req1Name")
          .operation(new OperationSelection().id("AddSourceDataset").params(paramValues));
      when(sessDB.addRequest(req)).thenReturn(CompletableFuture.completedFuture(req));
      taskerSvc.submitRequest(req).get();
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Cannot convert"));
    }
  }

  @Test
  public void testSubmitCompleteRequest() throws InterruptedException, ExecutionException {
    HashMap<String, Object> paramValues = new HashMap<String, Object>();
    paramValues.put("inputUri", "hdfs://some/where/");
    paramValues.put("dataFormat", "TELEPHONE.CSV");
    
    Request req = new Request().sessionId("newSess").label("req1Name")
        .operation(new OperationSelection().id("addSourceDataset").params(paramValues));
    when(sessDB.addRequest(req)).thenReturn(CompletableFuture.completedFuture(req));
//    when(sessDB.addJob(Matchers.any(Job.class))).thenAnswer(new Answer<CompletableFuture<Job>>() {
//      @Override
//      public CompletableFuture<Job> answer(InvocationOnMock invocation) throws Throwable {
//        Object[] args = invocation.getArguments();
//        return CompletableFuture.completedFuture((Job) args[0]);
//      }
//    });
//    taskerSvc.submitRequest(req).get();
  }
}
