package dataengine.server;

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

import dataengine.api.NotFoundException;
import dataengine.api.Session;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.VerticleConsts;
import dataengine.sessions.TinkerGraphSessionsDbModule;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.ClusteredVertxInjectionModule;
import net.deelam.vertx.rpc.RpcVerticleServer;

@Slf4j
public class VertxRpcSessionsTest {

  private SessionsDB_I sessionsDbRpcClient;

  @Before
  public void before() throws InterruptedException, ExecutionException, TimeoutException {
    Thread clientThread = new Thread(() -> {
      // simulate REST server that uses SessionsDB RPC client 
      Supplier<SessionsDB_I> sessionsDbRpcClientS = DeServerGuiceInjector.singleton().getInstance(
          Key.get(new TypeLiteral<Supplier<SessionsDB_I>>() {}));
      sessionsDbRpcClient = sessionsDbRpcClientS.get();
    } , "SessionClient");

    Thread serverThread = new Thread(() -> { // set up service in another vertx
      CompletableFuture<Vertx> vertxF = new CompletableFuture<>();
      Injector injector = Guice.createInjector(
          new ClusteredVertxInjectionModule(vertxF, new ClusteredVertxConfig()),
          new TinkerGraphSessionsDbModule());

      Vertx vertx = injector.getInstance(Vertx.class);
      log.info("vertx={}", vertx);
      try {
        SessionsDB_I sessVert = injector.getInstance(SessionsDB_I.class);
        log.info("sessVert={}", sessVert);
        new RpcVerticleServer(vertx, VerticleConsts.sessionDbBroadcastAddr)
            .start("SessionsDBServiceBusAddr", sessVert);
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
    Session session = new Session().label(sessId);

    Session createdSession = sessionsDbRpcClient.createSession(session).get();
    log.info("create: " + createdSession);
    log.info("list: " + sessionsDbRpcClient.listSessionIds().get());
  }

}
