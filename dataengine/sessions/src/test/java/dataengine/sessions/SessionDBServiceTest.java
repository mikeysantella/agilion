package dataengine.sessions;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;

import dataengine.apis.SessionsDB_I;
import lombok.extern.slf4j.Slf4j;
import net.deelam.graph.GrafUri;
import net.deelam.graph.IdGrafFactoryTinker;

@Slf4j
public class SessionDBServiceTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  SessionsDB_I sess;

  @Before
  public void setUp() throws Exception {
    sess=createSessionDB();
  }

  static SessionsDB_I createSessionDB() throws IOException {
    IdGrafFactoryTinker.register();
    GrafUri sessGraphUri = new GrafUri("tinker:///");
    IdGraph<?> sessGraph = sessGraphUri.createNewIdGraph(true);
    return new SessionsDBService(new SessionDB(sessGraph));
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetDataset() {
    //fail("Not yet implemented");
  }

}
