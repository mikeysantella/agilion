package dataengine.sessions.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.Initializer;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(SessionFrame.TYPE_VALUE)
public interface SessionFrame extends BaseFrame {
  public static final String TYPE_VALUE = "SessionFrame";

  @Property("baseDir")
  void setBaseDirectory(String baseDir);

  @Property("baseDir")
  String getBaseDirectory();

  @Property("username")
  void setUsername(String username);

  @Property("username")
  String getUsername();

  @Adjacency(label = "request", direction = Direction.OUT)
  void addRequest(RequestFrame sData);

  @Adjacency(label = "request", direction = Direction.OUT)
  Iterable<RequestFrame> getRequests();

  
  abstract class Impl extends BaseFrame.Impl implements SessionFrame {
    @Initializer
    public void init() {
      super.init();
    }
  }

}
