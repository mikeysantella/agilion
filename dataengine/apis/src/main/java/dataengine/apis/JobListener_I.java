package dataengine.apis;

import java.util.concurrent.CompletableFuture;

import dataengine.api.Job;
import dataengine.api.Progress;
import dataengine.api.State;

public interface JobListener_I {

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
  
  /**
   * called by job performer or other client
   * @param job
   * @param inputJobIds
   * @return
   */
  CompletableFuture<Boolean> addJob(Job job, String... inputJobIds);
}
