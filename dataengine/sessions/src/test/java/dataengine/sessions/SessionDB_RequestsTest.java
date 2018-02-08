package dataengine.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;

import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.api.Session;
import dataengine.apis.SessionsDB_I;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionDB_RequestsTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  SessionsDB_I sess;

  @Before
  public void setUp() throws Exception {
    sess=SessionDBServiceTest.createSessionDB();
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testAddRequest() throws InterruptedException, ExecutionException {
    Session session = new Session().id("newSess").label("name 1");
    sess.createSession(session);
    {
      Request req = new Request().sessionId("newSess").id("req1").label("req1Name")
          .operation(new OperationSelection().id("selectedOp"));
      Request req2 = sess.addRequest(req).get();
      req2.setState(null); // ignore
      req2.setCreatedTime(null); // ignore
      if(req2.getOperation().getParams().isEmpty())
        req2.getOperation().setParams(null);
      req2.getJobs().clear();; // ignore
      assertEquals(req, req2);
    }

    {
      Map<String,Object> operationParams=Maps.newHashMap();
      operationParams.put("inputDS", "ds1");
      operationParams.put("outputDS", "ds2");
      Request req = new Request().sessionId("newSess").id("req1").label("req1Name")
          .operation(new OperationSelection().id("myOp").params(operationParams));
      try{
        sess.addRequest(req).get();
        fail("Expected exception");
      }catch(Exception e){
      }
      req.id("reqB");
      Request req2 =sess.addRequest(req).get();
      req2.setCreatedTime(null); // ignore
      req2.setState(null); // ignore
      req2.getJobs().clear();; // ignore
      assertEquals(req, req2);
      
      Request reqB =sess.getRequest(req.getId()).get();      
      reqB.setCreatedTime(null); // ignore
      reqB.setState(null); // ignore
      reqB.getJobs().clear();; // ignore
      assertEquals(req, reqB);
    }
  }

}
