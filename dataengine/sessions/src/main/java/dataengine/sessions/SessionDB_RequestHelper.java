package dataengine.sessions;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.deelam.graph.GrafTxn.tryAndCloseTxn;

import java.util.List;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.frames.FramedTransactionalGraph;

import dataengine.api.Request;
import dataengine.sessions.frames.RequestFrame;
import dataengine.sessions.frames.SessionFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class SessionDB_RequestHelper {

  private final FramedTransactionalGraph<TransactionalGraph> graph;
  private final SessionDB_SessionHelper sessHelper;
  private final SessionDB_FrameHelper frameHelper;

  public boolean hasRequest(String id) {
    return frameHelper.hasFrame(id, RequestFrame.TYPE_VALUE);
  }
  
  public RequestFrame getRequestFrame(String id) {
    return frameHelper.getVertexFrame(id, RequestFrame.class);
  }

  static final String REQUEST_PARAMS_PROPPREFIX = "params.";
  static int requestCounter = 0;

  public void addRequestNode(Request request) {
    log.debug("addRequestNode: {}", request.getId());
    tryAndCloseTxn(graph, graph -> {
      String sessId = request.getSessionId();
      SessionFrame sf = sessHelper.getSessionFrame(sessId);
      String reqId = request.getId();
      RequestFrame rf = graph.getVertex(reqId, RequestFrame.class);
      if (rf == null) {
        rf = graph.addVertex(reqId, RequestFrame.class);
        sf.addRequest(rf);
        if (request.getLabel() == null)
          rf.setLabel("request " + (++requestCounter));
        else
          rf.setLabel(request.getLabel());
        if (request.getCreatedTime() != null)
          rf.setCreatedDate((request.getCreatedTime()));
//          rf.setCreatedDate(VertexFrameHelper.toOffsetDateTime(request.getCreatedTime()));
        if (request.getOperationId() != null)
          rf.setOperation(request.getOperationId());
        if (request.getOperationParams() != null)
          SessionDB_FrameHelper.saveMapAsProperties(request.getOperationParams(),
              rf.asVertex(), REQUEST_PARAMS_PROPPREFIX);

        if (request.getJobs() != null && request.getJobs().size() > 0)
          log.warn("Ignoring jobs -- expecting jobs to be empty: {}", request);

      } else {
        throw new IllegalArgumentException("Request.id already exists: " + reqId);
      }
    });
  }

  public static Request toRequest(RequestFrame rf) {
    return new Request().id(rf.getNodeId())
        .label(rf.getLabel())
        .sessionId(rf.getSession().getNodeId())
        .operationId(rf.getOperation())
        .createdTime((rf.getCreatedDate()))
        .state(rf.getState())
        .operationParams(SessionDB_FrameHelper.loadPropertiesAsMap(rf.asVertex(), REQUEST_PARAMS_PROPPREFIX))
        .jobs(SessionDB_JobHelper.toJobs(rf.getJobs()));
  }

  static List<Request> toRequest(Iterable<RequestFrame> requests) {
    return stream(requests.spliterator(), false)
        .map(req -> toRequest(req))
        .collect(toList());
  }


}
