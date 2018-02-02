package dataengine.sessions;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.deelam.graph.GrafTxn.tryAndCloseTxn;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedTransactionalGraph;

import dataengine.api.Session;
import dataengine.sessions.frames.SessionFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.ConsoleLogging;

@Slf4j
@RequiredArgsConstructor
public final class SessionDB_SessionHelper {

  private final FramedTransactionalGraph<TransactionalGraph> graph;

  private final SessionDB_FrameHelper frameHelper;

  public boolean hasSession(String id) {
    return frameHelper.hasFrame(id, SessionFrame.TYPE_VALUE);
  }
  
  public SessionFrame getSessionFrame(String id) {
    log.debug("getSessionFrame: {}", id);
    return frameHelper.getVertexFrame(id, SessionFrame.class);
  }

  public boolean sessionIdExists(String sessId) {
    return tryAndCloseTxn(graph, graph -> graph.getVertex(sessId, SessionFrame.class) != null);
  }

  private static final String SESSION_LABEL = "session";
  private static final String SESSION_DEFAULTS_PROPPREFIX = "defaults.";

  private static int sessionCounter = 0;

  public void addSessionNode(Session session, String baseDir) {
    SessionDB.clog.info("SESS: addSessionNode: {} ({}) at baseDir={}", session.getLabel(), session.getId(), baseDir);
    tryAndCloseTxn(graph, graph -> {
      String id = session.getId();
      SessionFrame sf = graph.getVertex(id, SessionFrame.class);
      if (sf == null) {
        sf = graph.addVertex(id, SessionFrame.class);
        graph.addEdge(id, graph.getVertex(SessionDB.ROOT_NODE), sf.asVertex(), SESSION_LABEL);
        sf.setBaseDirectory(baseDir);
        
        if (session.getLabel() == null)
          sf.setLabel("session " + (++sessionCounter));
        else
          sf.setLabel(SessionDB_FrameHelper.checkLabel(session.getLabel()));

        if (session.getCreatedTime() != null)
          sf.setCreatedDate((session.getCreatedTime()));
        //        sf.setCreatedDate(VertexFrameHelper.toOffsetDateTime(session.getCreatedTime()));
        if (session.getUsername() != null)
          sf.setUsername(session.getUsername());
        if (session.getDefaults() != null)
          SessionDB_FrameHelper.saveMapAsProperties(session.getDefaults(), sf.asVertex(), SESSION_DEFAULTS_PROPPREFIX);

        if (session.getRequests() != null && session.getRequests().size() > 0)
          log.warn("Ignoring requests -- expecting requests to be empty: {}", session);

      } else {
        log.warn("SessionId already exists: {}", id);
        throw new IllegalArgumentException("SessionId already exists: " + id);
      }
    });
  }

  public void modifySessionNode(String sId, Map<String, Object> props) {
    log.info("SESS: modifySessionNode: {}", sId, props);
    tryAndCloseTxn(graph, graph -> {
      String id = sId;
      SessionFrame qs = graph.getVertex(id, SessionFrame.class);
      if (qs == null) {
        log.warn("SessionId already exists: {}", id);
        throw new IllegalArgumentException("SessionId already exists: " + id);
      } else {
        props.forEach((k, val) -> {
          if (val != null)
            switch (k.toLowerCase()) {
              case "label":
                if (val instanceof String)
                  qs.setLabel(SessionDB_FrameHelper.checkLabel((String) val));
                else if (val instanceof Number)
                  qs.setLabel(SessionDB_FrameHelper.checkLabel(val.toString()));
                else
                  log.warn("Expecting String for key={} but got: {}", k, val.getClass());
                break;
              case "createdtime":
                if (val instanceof OffsetDateTime)
                  qs.setCreatedDate((OffsetDateTime) val);
                else if (val instanceof String)
                  qs.setCreatedDate(OffsetDateTime.parse((String) val));
                else if (val instanceof Long)
                  qs.setCreatedDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli((Long) val), ZoneId.of("Z")));
                else
                  log.warn("Expecting OffsetDateTime, String, or long for key={} but got: {}", k, val.getClass());
                break;
              case "username":
                if (val instanceof String)
                  qs.setUsername((String) val);
                else
                  log.warn("Expecting String for key={} but got: {}", k, val.getClass());
                break;
              default:
                SessionDB_FrameHelper.setVertexProperty(qs.asVertex(), SESSION_DEFAULTS_PROPPREFIX, k, val);
                break;
            }
        });
      }
    });
  }

  public static Session toSession(SessionFrame sf) {
    Session sess = new Session().id(sf.getNodeId())
        .label(sf.getLabel())
        .username(sf.getUsername())
        .createdTime((sf.getCreatedDate()))
        //        .createdTime(VertexFrameHelper.toJodaDateTime(sf.getCreatedDate()))
        .defaults(SessionDB_FrameHelper.loadPropertiesAsMap(sf.asVertex(), SESSION_DEFAULTS_PROPPREFIX))
        .requests(SessionDB_RequestHelper.toRequests(sf.getRequests()));
    log.trace("toSession={}", sess);
    return sess;
  }
  
  static List<Session> toSessions(Iterable<SessionFrame> sessions) {
    return stream(sessions.spliterator(),false)
        .sorted(SessionDB_FrameHelper.createdTimeComparator)
        .map(req -> toSession(req))
        .collect(toList());
  }

  public Map<String, String> getSessionIdsWithProperty(String propForValue) {
    return tryAndCloseTxn(graph, graph -> {
      Map<String, String> sessIds = new HashMap<>();
      Vertex root = graph.getVertex(SessionDB.ROOT_NODE);
      for (Vertex v : root.getVertices(Direction.OUT, SESSION_LABEL)) {
        if (propForValue == null)
          sessIds.put((String) v.getId(), null);
        else
          sessIds.put((String) v.getId(), v.getProperty(propForValue));
      }
      return sessIds;
    });
  }

  public List<SessionFrame> listSessionFrames() {
    return tryAndCloseTxn(graph, graph -> {
      List<SessionFrame> sessIds = new ArrayList<>();
      Vertex root = graph.getVertex(SessionDB.ROOT_NODE);
      for (Vertex v : root.getVertices(Direction.OUT, SESSION_LABEL)) {
        sessIds.add(graph.getVertex(v.getId(), SessionFrame.class));
      }
      return sessIds;
    });
  }

  ///

}
