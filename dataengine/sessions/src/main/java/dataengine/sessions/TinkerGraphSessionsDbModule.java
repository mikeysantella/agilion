package dataengine.sessions;

import java.io.IOException;

import com.google.inject.AbstractModule;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

import dataengine.apis.SessionsDB_I;
import net.deelam.graph.GrafUri;
import net.deelam.graph.IdGrafFactoryTinker;

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
  }
}