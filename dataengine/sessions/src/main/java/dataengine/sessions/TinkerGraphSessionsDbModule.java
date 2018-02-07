package dataengine.sessions;

import java.io.IOException;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.CommunicationConsts;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.ActiveMqRpcServer;
import net.deelam.activemq.rpc.AmqComponentSubscriber;
import net.deelam.graph.GrafUri;
import net.deelam.graph.IdGrafFactoryTinker;

@Slf4j
public class TinkerGraphSessionsDbModule extends AbstractModule {

  @Override
  protected void configure() {
    requireBinding(Connection.class);
    
    IdGrafFactoryTinker.register();
    GrafUri sessGraphUri = new GrafUri("tinker:///");
    try {
      IdGraph<?> sessGraph = sessGraphUri.createNewIdGraph(true);
      SessionsDBService sessDbSvc = new SessionsDBService(new SessionDB(sessGraph));
      bind(SessionsDB_I.class).toInstance(sessDbSvc);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      Thread shutdownSessionGraphThread=new Thread(()->{
        sessGraphUri.shutdown();
      },"mySessionGraphShutdown");
      Runtime.getRuntime().addShutdownHook(shutdownSessionGraphThread);
    }
    log.info("TinkerGraphSessionsDbModule configured");
  }
  
  static void deploySessionDb(Injector injector, int deliveryMode) throws JMSException {
    SessionsDB_I sessDbSvc = injector.getInstance(SessionsDB_I.class);
    log.info("AMQ: SERV: Deploying RPC service for SessionsDB_I: {} ", sessDbSvc); 
    injector.getInstance(ActiveMqRpcServer.class).start(CommunicationConsts.SESSIONDB_RPCADDR, sessDbSvc, deliveryMode/*, true*/);
    
    Connection connection=injector.getInstance(Connection.class);
    new AmqComponentSubscriber(connection, deliveryMode, "SessionsDB", 
        CommunicationConsts.RPC_ADDR, CommunicationConsts.SESSIONDB_RPCADDR,
        CommunicationConsts.COMPONENT_TYPE, "SessionsDB");
  }
}