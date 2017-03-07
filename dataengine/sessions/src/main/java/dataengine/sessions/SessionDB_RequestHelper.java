package dataengine.sessions;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.deelam.graph.GrafTxn.tryAndCloseTxn;

import java.util.List;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.frames.FramedTransactionalGraph;

import dataengine.api.Request;
import dataengine.sessions.frames.DatasetFrame;
import dataengine.sessions.frames.RequestFrame;
import dataengine.sessions.frames.SessionFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class SessionDB_RequestHelper {

  private final FramedTransactionalGraph<TransactionalGraph> graph;
  private final SessionDB_SessionHelper sessHelper;
  private final SessionDB_OperationHelper opsHelper;
  private final SessionDB_FrameHelper frameHelper;

  public boolean hasRequest(String id) {
    return frameHelper.hasFrame(id, RequestFrame.TYPE_VALUE);
  }
  
  public RequestFrame getRequestFrame(String id) {
    return frameHelper.getVertexFrame(id, RequestFrame.class);
  }

  static int requestCounter = 0;

  // TODO: 1: add priorRequests
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
          rf.setLabel(SessionDB_FrameHelper.checkLabel(request.getLabel()));
        if (request.getCreatedTime() != null)
          rf.setCreatedDate((request.getCreatedTime()));
//          rf.setCreatedDate(VertexFrameHelper.toOffsetDateTime(request.getCreatedTime()));
        
        if(request.getOperation()==null)
          throw new IllegalArgumentException("Request.operation must not be null! "+request);
        rf.setOperation(opsHelper.addOperationNode(
            rf.getNodeId()+"."+request.getOperation().getId(), request.getOperation()));
//        for(OperationSelection op:request.getOperations()){
//          rf.addOperation(opsHelper.addOperationNode(rf.getNodeId()+"."+op.getId(), op));
//        };

        if (request.getJobs() != null && request.getJobs().size() > 0)
          log.warn("Ignoring jobs -- expecting jobs to be empty: {}", request);

      } else {
        throw new IllegalArgumentException("Request.id already exists: " + reqId);
      }
    });
  }
  
  public void connectAsOutputDatasetNode(String requestId, String datasetId){
    log.debug("connectAsOutputDatasetNode: reqId={} dsId={}", requestId, datasetId);
    tryAndCloseTxn(graph, graph -> {
      RequestFrame rf = frameHelper.getVertexFrame(requestId, RequestFrame.class);
      DatasetFrame df = frameHelper.getVertexFrame(datasetId, DatasetFrame.class);
      rf.addOutputDataset(df);
    });
  }

  public static Request toRequest(RequestFrame rf) {
    return new Request().id(rf.getNodeId())
        .label(rf.getLabel())
        .sessionId(rf.getSession().getNodeId())
        .createdTime((rf.getCreatedDate()))
        .state(rf.getState())
        .operation(SessionDB_OperationHelper.toOperationSelection(rf.getOperation()))
        .jobs(SessionDB_JobHelper.toJobs(rf.getJobs()));
  }

  static List<Request> toRequests(Iterable<RequestFrame> requests) {
    return stream(requests.spliterator(),false)
        .sorted(SessionDB_FrameHelper.createdTimeComparator)
        .map(req -> toRequest(req))
        .collect(toList());
  }


}
