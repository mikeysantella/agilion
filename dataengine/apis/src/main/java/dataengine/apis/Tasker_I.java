package dataengine.apis;

import java.util.concurrent.CompletableFuture;

import dataengine.api.Progress;
import dataengine.api.Request;
import dataengine.api.State;

public interface Tasker_I {
  
  CompletableFuture<Request> submitRequest(Request req);

  // called by job performer
  void updateJobState(String jobId, State state);
  
  // called by job performer
  void updateJobProgress(String jobId, Progress progress);
}
