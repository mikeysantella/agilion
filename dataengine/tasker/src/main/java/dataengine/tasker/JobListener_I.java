package dataengine.tasker;

import java.util.Properties;
import java.util.function.BiConsumer;
import javax.jms.Message;
import dataengine.apis.ProgressState;

public interface JobListener_I {

  //public String METRICS_KEY_JOB = "job";

  String getEventBusAddress();
  
  public static interface Factory {
    JobListener_I create(Properties configMap, BiConsumer<Message,ProgressState> handler); 
  }
}
