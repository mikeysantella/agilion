package dataengine.sessions.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.Initializer;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 */
@TypeValue(DatasetFrame.TYPE_VALUE)
public interface DatasetFrame extends BaseFrame {
  public static final String TYPE_VALUE = "Dataset";

  @Property("uri")
  void setUri(String graphUri);

  @Property("uri")
  String getUri();

  @Property("schema")
  void setDataSchema(String schema);

  @Property("schema")
  String getDataSchema();

  @Property("dataFormat") // dataset storage format, e.g., csv-semicolon
  String getDataFormat();

  @Property("dataFormat")
  void setDataFormat(String format);

  @Adjacency(label = JobFrame.OUTPUT_DATA, direction = Direction.IN)
  Iterable<JobFrame> getCreatorTasks();

  @Adjacency(label = JobFrame.INPUT_DATA, direction = Direction.IN)
  Iterable<JobFrame> getDownstreamTasks();

  abstract class DataFrameImpl extends BaseFrame.Impl implements DatasetFrame {
    @Initializer
    public void init() {
      super.init();
    }
  }

}
