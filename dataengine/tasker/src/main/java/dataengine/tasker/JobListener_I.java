package dataengine.tasker;

public interface JobListener_I {

  public String METRICS_KEY_JOB = "job";

  String getEventBusAddress();
  
  int getProgressPollIntervalSeconds();

}
