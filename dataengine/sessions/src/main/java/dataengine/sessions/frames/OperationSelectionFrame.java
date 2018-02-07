package dataengine.sessions.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.Initializer;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(OperationSelectionFrame.TYPE_VALUE)
public interface OperationSelectionFrame extends BaseFrame {
  public static final String TYPE_VALUE = "OperationFrame";

  @Property("operationId")
  String getOperationId();

  @Property("operationId")
  void setOperationId(String opId);

  @Adjacency(label = "suboperation", direction = Direction.OUT)
  void addSubOperation(OperationSelectionFrame os);

  @Adjacency(label = "suboperation", direction = Direction.OUT)
  Iterable<OperationSelectionFrame> getSubOperations();

  // must be called "Impl"
  abstract class Impl extends BaseFrame.Impl implements OperationSelectionFrame {
    @Initializer
    public void init() {
      super.init();
    }
  }

}
