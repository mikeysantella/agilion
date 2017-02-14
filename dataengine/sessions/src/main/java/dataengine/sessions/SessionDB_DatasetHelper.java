package dataengine.sessions;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static net.deelam.graph.GrafTxn.tryAndCloseTxn;

import java.util.List;
import java.util.Map;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.frames.FramedTransactionalGraph;

import dataengine.api.Dataset;
import dataengine.api.Job;
import dataengine.sessions.frames.DatasetFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class SessionDB_DatasetHelper {
  private final FramedTransactionalGraph<TransactionalGraph> graph;
  private final SessionDB_SessionHelper sessHelper;
  private final SessionDB_FrameHelper frameHelper;

  public DatasetFrame getDatasetFrame(String id) {
    return frameHelper.getVertexFrame(id, DatasetFrame.class);
  }

  public void addDatasetNode(Dataset job) {
    log.debug("addJobNode: {}", job.getId());
    tryAndCloseTxn(graph, graph -> {
      String reqId = job.getJobId();
      // TODO: complete
    });
  }
  
  private static final String STATS_PROPPREFIX = "stats.";

  public static List<Dataset> toDatasets(Iterable<DatasetFrame> datasets) {
    return stream(datasets.spliterator(), false)
        .map(req -> toDataset(req))
        .collect(toList());
  }
  
  public static Map<String,String> toDatasetMap(Iterable<DatasetFrame> datasets) {
    return stream(datasets.spliterator(), false)
        .collect(toMap(DatasetFrame::getLabel,DatasetFrame::getNodeId));
  }

  public static Dataset toDataset(DatasetFrame df) {
    return new Dataset().id(df.getNodeId())
        .label(df.getLabel())
        .jobId(df.getCreatorTask().getNodeId())
        .createdTime((df.getCreatedDate()))
        //.createdTime(VertexFrameHelper.toJodaDateTime(df.getCreatedDate()))
        .state(df.getState())
        .uri(df.getUri())
        .dataFormat(df.getDataFormat())
        .dataSchema(df.getSchema())
        .stats(SessionDB_FrameHelper.loadPropertiesAsMap(df.asVertex(), STATS_PROPPREFIX));
  }
}
