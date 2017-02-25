package dataengine.sessions.frames;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.javahandler.Initializer;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

import dataengine.api.State;
import lombok.extern.slf4j.Slf4j;
import net.deelam.graph.BpPropertySerializerUtils;

@TypeField(BaseFrame.TYPE_KEY)
public interface BaseFrame extends VertexFrame {

  //***** Constants *********************
  static final String TYPE_KEY = "_FRAME_TYPE";

  //--------- Properties -------------------

  // property key should match IdGraph.ID
  @JavaHandler
  String getNodeId();

  @Property("label")
  void setLabel(String name);

  @Property("label")
  String getLabel();
  

  @Property("state")
  void setState(State state);

  @Property("state")
  State getState();
  
  @JavaHandler
  OffsetDateTime getCreatedDate();

  @JavaHandler
  void setCreatedDate(OffsetDateTime createDate);

  //--------- Operations --------------

  //	@JavaHandler
  //	String toDebugString();

  //	@JavaHandler
  //	void copyProperties(ReqMgrBaseConcept concept);

  @JavaHandler
  void delete();

  // must be called "Impl"
  abstract class Impl implements BaseFrame {
    private static final ZoneId UTC = ZoneId.of("Z");

    @Initializer
    public void init() {
      setState(State.CREATED);
      setCreatedDate(OffsetDateTime.now().atZoneSameInstant(UTC).toOffsetDateTime());
    }
    
    public String getNodeId() {
      return asVertex().getId().toString();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof VertexFrame) {
        return asVertex().equals(((VertexFrame) obj).asVertex());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return asVertex().hashCode();
    }

    @Override
    public void delete() {
      this.asVertex().remove();
    }

    //		@Override
    //		public void copyProperties(ReqMgrBaseConcept concept){
    //			for(String key:concept.asVertex().getPropertyKeys()){
    //				if(ReservedPropertyKeys.isReserved(key))
    //					continue;
    //				asVertex().setProperty(key, concept.asVertex().getProperty(key));
    //			}
    //		}
    //
    //		@Override
    //		public String toDebugString(){
    //			StringBuilder sb=new StringBuilder(getNodeId());
    //			sb.append(" :: ");
    //			for(String key:asVertex().getPropertyKeys()){
    //				sb.append(key).append(":").append(asVertex().getProperty(key).toString()).append(", ");
    //			}
    //			return sb.toString();
    //		}
    //		
    //		public static String getFrameType(Vertex v){
    //			String type=v.getProperty(TYPE_KEY);
    //			return type;
    //		}

    private OffsetDateTime createdDate;

    @Override
    public OffsetDateTime getCreatedDate() {
      if (createdDate == null) {
        createdDate = BpPropertySerializerUtils.getOffsetDateTime(asVertex(), "createdTime");
      }
      return createdDate;
    }

    @Override
    public void setCreatedDate(OffsetDateTime date) {
      createdDate = date;
      BpPropertySerializerUtils.setOffsetDateTime(asVertex(), "createdTime", createdDate);
    }

  }
}
