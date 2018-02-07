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
import dataengine.sessions.frames.DatasetFrame;
import dataengine.sessions.frames.JobFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class SessionDB_DatasetHelper {
  private final FramedTransactionalGraph<TransactionalGraph> graph;
  private final SessionDB_FrameHelper frameHelper;

  public boolean hasDataset(String id) {
    return frameHelper.hasFrame(id, DatasetFrame.TYPE_VALUE);
  }
  
  public DatasetFrame getDatasetFrame(String id) {
    return frameHelper.getVertexFrame(id, DatasetFrame.class);
  }

  static final String DATASET_STATS_PROPPREFIX = "stats.";
  
  enum IO {INPUT, OUTPUT}
  static int datasetCounter = 0;
  
  public void addDatasetNode(Dataset ds, String jobId, IO io) {
    SessionDB.clog.info("SESS: addDatasetNode: '{}' id={} as "+io.name()+" for jobId={}", ds.getLabel(), ds.getId(), jobId);
    tryAndCloseTxn(graph, graph -> {
      JobFrame jf = graph.getVertex(jobId, JobFrame.class);
      String dsId = ds.getId();
      DatasetFrame df = graph.getVertex(dsId, DatasetFrame.class);
      if (df == null) {
        df = graph.addVertex(dsId, DatasetFrame.class);
        switch(io){
          case INPUT:
            jf.addInputDataset(df);
            break;
          case OUTPUT:
            jf.addOutputDataset(df);
            break;
          default:
            throw new IllegalArgumentException("io="+io);
        }
        if (ds.getLabel() == null)
          df.setLabel("dataset " + (++datasetCounter));
        else
          df.setLabel(SessionDB_FrameHelper.checkLabel(ds.getLabel()));
        if (ds.getCreatedTime() != null)
          df.setCreatedDate((ds.getCreatedTime()));
        if (ds.getUri() != null)
          df.setUri((ds.getUri()));
        if (ds.getDataSchema() != null)
          df.setDataSchema((ds.getDataSchema()));
        if (ds.getDataFormat() != null)
          df.setDataFormat((ds.getDataFormat()));
        if (ds.getStats() != null)
          SessionDB_FrameHelper.saveMapAsProperties(ds.getStats(),
              df.asVertex(), DATASET_STATS_PROPPREFIX);
      } else {
        throw new IllegalArgumentException("Request.id already exists: " + dsId);
      }
    });
  }
  
  private static final String STATS_PROPPREFIX = "stats.";

  public static List<Dataset> toDatasets(Iterable<DatasetFrame> datasets) {
    return stream(datasets.spliterator(), false)
        .map(req -> toDataset(req))
        .collect(toList());
  }
  
  public static Map<String,String> toDatasetMap(Iterable<DatasetFrame> datasets) {
    if(datasets==null)
      return null;
    return stream(datasets.spliterator(), false)
        .collect(toMap(DatasetFrame::getLabel,DatasetFrame::getNodeId));
  }

  public static Dataset toDataset(DatasetFrame df) {
    return new Dataset().id(df.getNodeId())
        .label(df.getLabel())
        .createdTime((df.getCreatedDate()))
        //.createdTime(VertexFrameHelper.toJodaDateTime(df.getCreatedDate()))
        .state(df.getState())
        .uri(df.getUri())
        .dataFormat(df.getDataFormat())
        .dataSchema(df.getDataSchema())
        .stats(SessionDB_FrameHelper.loadPropertiesAsMap(df.asVertex(), STATS_PROPPREFIX));
  }
}
