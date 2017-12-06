package dataengine.server;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.jms.Connection;
import javax.jms.JMSException;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import dataengine.api.Job;
import dataengine.api.NotFoundException;
import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.api.Session;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.VerticleConsts;
import dataengine.sessions.TinkerGraphSessionsDbModule;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.MQService;
import net.deelam.activemq.rpc.ActiveMqRpcServer;

@Slf4j
@Ignore
public class VertxRpcSessionsTest {

  private SessionsDB_I sessionsDbRpcClient;
  private static final String brokerUrl="tcp://localhost:45678";
  BrokerService broker;

  @Before
  public void before() throws Exception {
    broker = MQService.createBrokerService("test", brokerUrl);

    Thread clientThread = new Thread(() -> {
      try {
        Connection connection=MQClient.connect(brokerUrl);
        connection.start();
        // simulate REST server that uses SessionsDB RPC client 
        Injector injector = Guice.createInjector(
            new VertxRpcClients4ServerModule(connection));
        RpcClientProvider<SessionsDB_I> sessionsDbRpcClientS = injector.getInstance(
            Key.get(new TypeLiteral<RpcClientProvider<SessionsDB_I>>() {}));
        sessionsDbRpcClient = sessionsDbRpcClientS.rpc();
      } catch (JMSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } , "SessionClient");

    Thread serverThread = new Thread(() -> { // set up service in another vertx
      Injector injector = Guice.createInjector(
          new TinkerGraphSessionsDbModule());


      try {
        Connection connection=MQClient.connect(brokerUrl);
        connection.start();
        
        SessionsDB_I sessVert = injector.getInstance(SessionsDB_I.class);
        log.info("sessVert={}", sessVert);
//        new RpcVerticleServer(vertx, VerticleConsts.sessionDbBroadcastAddr)
//            .start("SessionsDBServiceBusAddr", sessVert, true);
        new ActiveMqRpcServer(connection).start(VerticleConsts.sessionDbBroadcastAddr, sessVert, true);
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
  
  @After
  public void after() {
    try {
      broker.stop();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void test() throws NotFoundException, InterruptedException, ExecutionException {
    String sessId = "newSess";
    Session session = new Session().id(sessId);

    Session createdSession = sessionsDbRpcClient.createSession(session).get();
    log.info("create: " + createdSession);
    log.info("list: " + sessionsDbRpcClient.listSessionIds().get());

    {
      Request req = new Request().sessionId(sessId).id("req1").label("req1Name")
          .operation(new OperationSelection().id("AddSourceDataset"));
      Request req2 = sessionsDbRpcClient.addRequest(req).get();
      req2.setState(null); // ignore
      req2.setCreatedTime(null); // ignore
      if(req2.getOperation().getParams().isEmpty())
        req2.getOperation().params(null);
      req2.getJobs().clear();; // ignore
      assertEquals(req, req2);
    }
    {
      Job job = new Job().requestId("req1").id("req1.jobA").label("jobAName");
      Job job2 = sessionsDbRpcClient.addJob(job).get();
      //log.info("job2={}", job2);
      job2.setState(null); // ignore
      job2.setCreatedTime(null); // ignore
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
