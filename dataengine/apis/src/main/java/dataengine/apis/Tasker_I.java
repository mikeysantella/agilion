package dataengine.apis;

import java.util.concurrent.CompletableFuture;

import dataengine.api.Job;
import dataengine.api.Progress;
import dataengine.api.Request;
import dataengine.api.State;

public interface Tasker_I {

  /**
   * called by REST server typically when OperationsRegistry are refreshed
   */
  CompletableFuture<Void> refreshJobsCreators();
  
  /**
   * called by REST server
   * @param req
   * @return
   */
  CompletableFuture<Request> submitRequest(Request req);

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
  CompletableFuture<Job> addJob(Job job, String... inputJobIds);
}
