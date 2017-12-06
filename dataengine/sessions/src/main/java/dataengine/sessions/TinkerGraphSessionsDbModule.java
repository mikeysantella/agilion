package dataengine.sessions;

import java.io.IOException;
import javax.jms.Connection;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.CommunicationConsts;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.ActiveMqRpcServer;
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
      });
      Runtime.getRuntime().addShutdownHook(shutdownSessionGraphThread);
    }
    log.info("TinkerGraphSessionsDbModule configured");
  }
  
  static void deploySessionDb(Injector injector) {
    SessionsDB_I sessDbSvc = injector.getInstance(SessionsDB_I.class);
    log.info("AMQ: SERV: Deploying RPC service for SessionsDB_I: {} ", sessDbSvc); 
    injector.getInstance(ActiveMqRpcServer.class).start(CommunicationConsts.sessionDbBroadcastAddr, sessDbSvc/*, true*/);
  }
}