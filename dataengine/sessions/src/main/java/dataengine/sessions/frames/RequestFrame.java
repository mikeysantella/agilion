package dataengine.sessions.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.Initializer;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(RequestFrame.TYPE_VALUE)
public interface RequestFrame extends BaseFrame {
  public static final String TYPE_VALUE = "RequestFrame";

  @Property("operation")
  String getOperation();

  @Property("operation")
  void setOperation(String operation);
  
  @Adjacency(label = "request", direction = Direction.IN)
  SessionFrame getSession();

  @Adjacency(label = "job", direction = Direction.OUT)
  void addJob(JobFrame job);

  @Adjacency(label = "job", direction = Direction.OUT)
  Iterable<JobFrame> getJobs();

  // must be called "Impl"
  abstract class Impl extends BaseFrame.Impl implements RequestFrame {
    @Initializer
    public void init() {
      super.init();
    }
  }



}
