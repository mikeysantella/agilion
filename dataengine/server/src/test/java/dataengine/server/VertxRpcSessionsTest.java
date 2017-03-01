package dataengine.server;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import dataengine.api.Job;
import dataengine.api.NotFoundException;
import dataengine.api.Request;
import dataengine.api.Session;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.VerticleConsts;
import dataengine.sessions.TinkerGraphSessionsDbModule;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxInjectionModule;
import net.deelam.vertx.rpc.RpcVerticleServer;
import net.deelam.vertx.rpc.VertxRpcClientsModule;

@Slf4j
public class VertxRpcSessionsTest {

  private SessionsDB_I sessionsDbRpcClient;

  @Before
  public void before() throws InterruptedException, ExecutionException, TimeoutException {
    CompletableFuture<Vertx> vertxF = new CompletableFuture<>();
    vertxF.complete(Vertx.vertx());

    Thread clientThread = new Thread(() -> {
      // simulate REST server that uses SessionsDB RPC client 
      Injector injector = Guice.createInjector(
          new VertxRpcClients4ServerModule(vertxF));
      Supplier<SessionsDB_I> sessionsDbRpcClientS = injector.getInstance(
          Key.get(new TypeLiteral<Supplier<SessionsDB_I>>() {}));
      sessionsDbRpcClient = sessionsDbRpcClientS.get();
    } , "SessionClient");

    Thread serverThread = new Thread(() -> { // set up service in another vertx
      Injector injector = Guice.createInjector(
          new ClusteredVertxInjectionModule(vertxF),
          new TinkerGraphSessionsDbModule());

      Vertx vertx = injector.getInstance(Vertx.class);
      log.info("vertx={}", vertx);
      try {
        SessionsDB_I sessVert = injector.getInstance(SessionsDB_I.class);
        log.info("sessVert={}", sessVert);
        new RpcVerticleServer(vertx, VerticleConsts.sessionDbBroadcastAddr)
            .start("SessionsDBServiceBusAddr", sessVert, true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } , "SessionSvc");

    clientThread.start();
    Thread.sleep(2000);
    serverThread.start();

    serverThread.join();
    clientThread.join();

    log.info("==============================");
  }

  @Test
  public void test() throws NotFoundException, InterruptedException, ExecutionException {
    String sessId = "newSess";
    Session session = new Session().id(sessId);

    Session createdSession = sessionsDbRpcClient.createSession(session).get();
    log.info("create: " + createdSession);
    log.info("list: " + sessionsDbRpcClient.listSessionIds().get());

    {
      Request req = new Request().sessionId(sessId).id("req1").label("req1Name");
      Request req2 = sessionsDbRpcClient.addRequest(req).get();
      req2.setState(null); // ignore
      req2.setCreatedTime(null); // ignore
      if (req2.getOperationParams().isEmpty())
        req2.setOperationParams(null); // ignore
      req2.getJobs().clear();; // ignore
      assertEquals(req, req2);
    }
    {
      Job job = new Job().requestId("req1").id("req1.jobA").label("jobAName");
      Job job2 = sessionsDbRpcClient.addJob(job).get();
      //log.info("job2={}", job2);
      job2.setState(null); // ignore
      job2.setProgress(null); // ignore
      if (job2.getParams().isEmpty())
        job2.setParams(null); // ignore
      if (job2.getInputDatasetIds().isEmpty())
        job2.setInputDatasetIds(null); // ignore
      if (job2.getOutputDatasetIds().isEmpty())
        job2.setOutputDatasetIds(null); // ignore
      assertEquals(job, job2);
    }
  }

}
