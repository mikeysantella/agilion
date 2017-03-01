package dataengine.tasker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
import dataengine.api.Request;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.DepJobService_I;

@Slf4j
public class TaskerServiceTest {

  @Before
  public void setUp() throws Exception {
    CompletableFuture<Vertx> vertxF = CompletableFuture.completedFuture(Vertx.vertx());
    Injector injector = Guice.createInjector(
        new TaskerModule(),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Vertx.class).toInstance(vertxF.join());

            // OperationsRegistryVerticle to which operations are registered by providers (ie, Workers)
            OperationsRegistryVerticle opsRegVert = new OperationsRegistryVerticle(VerticleConsts.opsRegBroadcastAddr);
            bind(OperationsRegistryVerticle.class).toInstance(opsRegVert);
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
    opsReg = injector.getInstance(OperationsRegistryVerticle.class);
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
              .key(OperationConsts.INGEST_DATAFORMAT).required(true)
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
  OperationsRegistryVerticle opsReg;
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
          .operationId("addSourceDataset");
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
      Request req = new Request().sessionId("newSess").label("req1Name")
          .operationId("addSourceDataset");
      HashMap<String, Object> paramValues = new HashMap<String, Object>();
      req.operationParams(paramValues);
      paramValues.put("inputUri", "hdfs://some/where/");
      paramValues.put("dataFormat", "SOME_UNKNOWN_FORMAT");
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
      Request req = new Request().sessionId("newSess").label("req1Name")
          .operationId("addSourceDataset");
      HashMap<String, Object> paramValues = new HashMap<String, Object>();
      req.operationParams(paramValues);
      paramValues.put("inputUri", "hdfs://some/where/");
      paramValues.put("dataFormat", 123);
      when(sessDB.addRequest(req)).thenReturn(CompletableFuture.completedFuture(req));
      taskerSvc.submitRequest(req).get();
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Cannot convert"));
    }
  }

  @Test
  public void testSubmitCompleteRequest() throws InterruptedException, ExecutionException {
    Request req = new Request().sessionId("newSess").label("req1Name")
        .operationId("addSourceDataset");
    HashMap<String, Object> paramValues = new HashMap<String, Object>();
    req.operationParams(paramValues);
    paramValues.put("inputUri", "hdfs://some/where/");
    paramValues.put("dataFormat", "TELEPHONE.CSV");
    when(sessDB.addRequest(req)).thenReturn(CompletableFuture.completedFuture(req));
    taskerSvc.submitRequest(req).get();
  }
}
