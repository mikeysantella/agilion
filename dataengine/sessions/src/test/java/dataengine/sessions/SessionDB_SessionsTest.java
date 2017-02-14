package dataengine.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;

import dataengine.api.Session;
import dataengine.apis.SessionsDB_I;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionDB_SessionsTest {

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
  public void testCreateSession() throws InterruptedException, ExecutionException {
    Session session = new Session().id("newSess");
    sess.createSession(session);
    Session session2 = sess.getSession("newSess").get();
    log.info(OffsetDateTime.now()+" "+session2);
    session2.setLabel(null); // ignore
    session2.setCreatedTime(null); // ignore
    session2.setDefaults(null); // ignore
    assertEquals(session, session2);
  }

  @Test
  public void testCreateSessionWithSameId() throws InterruptedException, ExecutionException {
    Session session = new Session().id("newSess");
    sess.createSession(session);
    Session sessionB = new Session().id("newSess");
    try {
      sess.createSession(sessionB);
      fail("Expected exception");
    } catch (Exception e) {
      log.info("Got expected: "+e);
    }

    Session session2 = sess.getSession("newSess").get();
    session2.setLabel(null); // ignore
    session2.setCreatedTime(null); // ignore
    session2.setDefaults(null); // ignore
    assertEquals(session, session2);
  }

  @Test
  public void testCreateSessionWithSameName() throws InterruptedException, ExecutionException {
    Session session = new Session().label("newSess");
    sess.createSession(session);
    Session sessionB = new Session().label("newSess");
    sess.createSession(sessionB);

    Session session2 = sess.getSession(session.getId()).get();
    session2.setCreatedTime(null); // ignore
    session2.setDefaults(null); // ignore
    assertEquals(session, session2);
    assertNotEquals(sessionB, session2);

    Session sessionB2 = sess.getSession(sessionB.getId()).get();
    sessionB2.setCreatedTime(null); // ignore
    sessionB2.setDefaults(null); // ignore
    assertEquals(sessionB, sessionB2);
  }

  @Test
  public void testSetMetadata() throws InterruptedException, ExecutionException {
    Session session = new Session().id("newSess");
    sess.createSession(session);
    Map<String, Object> props = Maps.newHashMap();
    {
      props.put("label", "new name");
      Session modSess = sess.setMetadata("newSess", props).get();
      assertEquals("new name", modSess.getLabel());
    }
    {
      props.clear();
      props.put("Label", "new Name");
      Session modSess = sess.setMetadata("newSess", props).get();
      assertEquals("new Name", modSess.getLabel());
    }
    {
      props.clear();
      props.put("LabeL", "new NamE");
      Session modSess = sess.setMetadata("newSess", props).get();
      assertEquals("new NamE", modSess.getLabel());
    }
    {
      props.clear();
      props.put("LaBeL", null);
      Session modSess = sess.setMetadata("newSess", props).get(); // ignored
      assertEquals("new NamE", modSess.getLabel()); // unchanged
    }
    {
      props.clear();
      props.put("Label", 123);
      Session modSess = sess.setMetadata("newSess", props).get();
      assertEquals("123", modSess.getLabel());
    }

    {
      props.clear();
      OffsetDateTime d = OffsetDateTime.now();
      props.put("createdTime", d);
      Session modSess = sess.setMetadata("newSess", props).get();
      assertEquals(d.toString(), modSess.getCreatedTime().toString());
    }
    {
      props.clear();
      OffsetDateTime d = OffsetDateTime.now();
      props.put("createdTime", d.toString());
      Session modSess = sess.setMetadata("newSess", props).get();
      assertEquals(d.toString(), modSess.getCreatedTime().toString());
    }
    {
      props.clear();
      OffsetDateTime d = OffsetDateTime.now().atZoneSameInstant(ZoneId.of("Z")).toOffsetDateTime();
      {
        props.put("createdTime", d.toInstant().toEpochMilli());
        Session modSess = sess.setMetadata("newSess", props).get();
        assertEquals(d.toString(), modSess.getCreatedTime().toString());
      }

      {
        props.clear();
        props.put("createdTime", 123f);
        Session modSess2 = sess.setMetadata("newSess", props).get(); // ignored
        assertEquals(d.toString(), modSess2.getCreatedTime().toString()); //unchanged
      }
    }

    {
      props.clear();
      props.put("userNamE", "agilion");
      Session modSess = sess.setMetadata("newSess", props).get();
      assertEquals("agilion", modSess.getUsername());
    }

    {
      props.clear();
      URI uri = URI.create("file:///path/to/my/file");
      props.put("myUri", uri);
      Session modSess = sess.setMetadata("newSess", props).get(); // Storing as string
      //System.out.println(modSess);
      assertEquals(uri.toString(), modSess.getDefaults().get("myUri"));
    }
    {
      props.clear();
      OffsetDateTime d = OffsetDateTime.now();
      props.put("myDate", d);
      Session modSess = sess.setMetadata("newSess", props).get(); // Storing as string
      //System.out.println(modSess);
      assertEquals(d.toString(), modSess.getDefaults().get("myDate"));
    }
  }

  @Test
  public void testListSessions() throws InterruptedException, ExecutionException {
    Session session = new Session().id("newSess").label("name 1");
    sess.createSession(session);
    Session session2 = new Session().id("newSess2").label("name 2");
    sess.createSession(session2);

    List<Session> list = sess.listSessions().get();
    assertEquals(2, list.size());

    {
      List<String> sessionIds = list.stream().map(s -> s.getId()).collect(Collectors.toList());
      assertTrue(sessionIds.contains("newSess"));
      assertTrue(sessionIds.contains("newSess2"));
    }
    {
      List<String> sessionIds = sess.listSessionIds().get();
      assertTrue(sessionIds.contains("newSess"));
      assertTrue(sessionIds.contains("newSess2"));
    }

    {
      List<String> sessionNames = list.stream().map(s -> s.getLabel()).collect(Collectors.toList());
      assertTrue(sessionNames.contains("name 1"));
      assertTrue(sessionNames.contains("name 2"));
    }

    {
      Map<String, String> sessionNames = sess.listSessionNames().get();
      log.info("sessionNames=" + sessionNames);
      assertEquals("name 1", sessionNames.get("newSess"));
      assertEquals("name 2", sessionNames.get("newSess2"));
    }
  }

}
