package dataengine.apis;

import dataengine.api.Progress;
import dataengine.api.State;

public interface JobListener_I {

  public String METRICS_KEY_JOB = "job";

  String getEventBusAddress();
  
  int getProgressPollIntervalSeconds();

  /**
   *  called by job performer
   * @param jobId
   * @param state
   */
  void updateJobState(String jobId, State state);
  
  /** 
   * called by job performer
   * @param jobId
   * @param progress
   */
  void updateJobProgress(String jobId, Progress progress);

}
