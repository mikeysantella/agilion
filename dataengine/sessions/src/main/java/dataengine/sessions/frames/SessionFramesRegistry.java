package dataengine.sessions.frames;

import java.util.LinkedList;
import java.util.List;

import com.tinkerpop.frames.VertexFrame;

public class SessionFramesRegistry {

  private static List<Class<? extends VertexFrame>> frameClasses = new LinkedList<Class<? extends VertexFrame>>();

  static {
    registerFrameClass(SessionFrame.class);
    registerFrameClass(RequestFrame.class);
    registerFrameClass(JobFrame.class);
    registerFrameClass(DatasetFrame.class);
  }

  public static void registerFrameClass(final Class<? extends VertexFrame> clazz) {
    frameClasses.add(clazz);
  }

  public static Class<?>[] getFrameClasses() {
    return frameClasses.toArray(new Class[frameClasses.size()]);
  }
}
