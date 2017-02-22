package dataengine.sessions.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.Initializer;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(JobFrame.TYPE_VALUE)
public interface JobFrame extends BaseFrame {
  public static final String TYPE_VALUE = "JobFrame";
  
  @Property("progress")
  int getProgress();

  @Property("progress")
  void setProgress(int percent);

  @Property("type")
  String getType();

  @Property("type")
  void setType(String dataType);

  @Adjacency(label ="job", direction = Direction.IN)
  RequestFrame getRequest();

  @Adjacency(label ="job", direction = Direction.IN)
  void setRequest(RequestFrame req);
  
  public static final String INPUT_DATA = "inputData";
  @Adjacency(label = INPUT_DATA, direction = Direction.OUT)
  void addInputDataset(DatasetFrame ds);

  @Adjacency(label = INPUT_DATA, direction = Direction.OUT)
  Iterable<DatasetFrame> getInputDatasets();
  
  public static final String OUTPUT_DATA = "outputData";
  @Adjacency(label = OUTPUT_DATA, direction = Direction.OUT)
  void addOutputDataset(DatasetFrame ds);

  @Adjacency(label = OUTPUT_DATA, direction = Direction.OUT)
  Iterable<DatasetFrame> getOutputDatasets();
  
  @Property("requestId")
  String getRequestId();
  @Property("requestId")
  void setRequestId(String requestId);

  abstract class JobFrameImpl extends BaseFrame.Impl implements JobFrame {

    @Initializer
    public void init() {
      super.init();
      setProgress(0);
    }
  }
  
}
