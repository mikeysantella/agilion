package dataengine.sessions;

import java.io.IOException;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

import dataengine.apis.SessionsDB_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.graph.GrafUri;
import net.deelam.graph.IdGrafFactoryTinker;
import net.deelam.vertx.rpc.RpcVerticleServer;

@Slf4j
public class TinkerGraphSessionsDbModule extends AbstractModule {
  @Override
  protected void configure() {            
    IdGrafFactoryTinker.register();
    GrafUri sessGraphUri = new GrafUri("tinker:///");
    try {
      IdGraph<?> sessGraph = sessGraphUri.createNewIdGraph(true);
      SessionsDBService sessVert = new SessionsDBService(new SessionDB(sessGraph));
      bind(SessionsDB_I.class).toInstance(sessVert);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      //sessGraphUri.shutdown(); // TODO: move to a shutdown hook
    }
    log.info("TinkerGraphSessionsDbModule configured");
  }

  static void deploySessionDb(Injector injector) {
    SessionsDB_I sessVert = injector.getInstance(SessionsDB_I.class);
    Vertx vertx = injector.getInstance(Vertx.class);
    new RpcVerticleServer(vertx, VerticleConsts.sessionDbBroadcastAddr)
        .start("SessionsDBServiceBusAddr", sessVert);
  }
}