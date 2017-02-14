package dataengine.sessions.frames;

import java.util.Date;

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

  public static final String TIMESTAMP = "timestamp";

  @Property(TIMESTAMP)
  void setTimestamp(String timestamp);

  @Property(TIMESTAMP)
  String getTimestamp();

  @Property("schema")
  void setSchema(String schema);

  @Property("schema")
  String getSchema();

  @Adjacency(label = JobFrame.OUTPUT_DATA, direction = Direction.IN)
  JobFrame getCreatorTask();

  abstract class DataFrameImpl extends BaseFrame.Impl implements DatasetFrame {
    @Initializer
    public void init() {
      //super.init();
      setTimestamp(new Date().toString());
    }
  }

  String getDataFormat();

  String getDataSchema();

}
