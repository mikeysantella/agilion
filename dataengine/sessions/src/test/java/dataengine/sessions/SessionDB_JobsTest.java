package dataengine.sessions;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    sess=SessionDBServiceTest.createSessionDB();
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testAddJob() {
//    fail("Not yet implemented");
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
