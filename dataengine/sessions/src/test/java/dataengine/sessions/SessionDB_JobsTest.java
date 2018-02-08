package dataengine.sessions;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dataengine.api.Job;
import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.api.Session;
import dataengine.apis.SessionsDB_I;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionDB_JobsTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  SessionsDB_I sess;

  @Before
  public void setUp() throws Exception {
    sess = SessionDBServiceTest.createSessionDB();
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testAddJob() throws InterruptedException, ExecutionException {
    Session session = new Session().id("newSess").label("name 1");
    sess.createSession(session);
    {
      Request req = new Request().sessionId("newSess").id("req1").label("req1Name")
          .operation(new OperationSelection().id("myOp"));
      Request req2 = sess.addRequest(req).get();
    }
    {
      Job job = new Job().requestId("req1").id("req1.jobA").label("jobAName");
      Job job2 = sess.addJob(job).get();
      log.info("job2={}", job2);
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

  @Test
  public void testUpdateJobState() {
    //    fail("Not yet implemented");
  }

  @Test
  public void testUpdateJobProgress() {
    //    fail("Not yet implemented");
  }

}
