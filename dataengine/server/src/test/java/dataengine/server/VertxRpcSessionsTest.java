package dataengine.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.api.NotFoundException;
import dataengine.api.Session;
import dataengine.api.SessionsApiService;
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

  private SessionsApiService sessionsApi;
  private SessionsDB_I sessionsDbRpcClient;

  @Before
  public void before() throws InterruptedException, ExecutionException {
    Thread t=new Thread(()->{
      // simulate REST server that uses SessionsDB RPC client 
      sessionsApi=DeServerGuiceInjector.singleton().getInstance(SessionsApiService.class);
      sessionsDbRpcClient=DeServerGuiceInjector.singleton().getInstance(SessionsDB_I.class);
    });
    t.start();

    { // set up service in another vertx
      CompletableFuture<Vertx> vertxF = new CompletableFuture<>();
      Injector injector = Guice.createInjector(
          new ClusteredVertxInjectionModule(vertxF, new ClusteredVertxConfig()),
          new TinkerGraphSessionsDbModule());
      
      
      SessionsDB_I sessVert = injector.getInstance(SessionsDB_I.class);
      Vertx vertx = vertxF.get();
      new RpcVerticleServer(vertx, VerticleConsts.sessionDbBroadcastAddr)
          .start("SessionsDBServiceBusAddr", sessVert);
    }

    t.join();
  }

  @Test
  public void test() throws NotFoundException, InterruptedException, ExecutionException {
    String sessId = "newSess";
    Session session = new Session().label(sessId);
    
    Session createdSession = sessionsDbRpcClient.createSession(session).get();
    log.info("create: "+createdSession);
    log.info("list: "+sessionsDbRpcClient.listSessionIds().get());
  }

}
