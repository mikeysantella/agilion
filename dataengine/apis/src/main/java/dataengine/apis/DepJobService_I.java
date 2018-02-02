package dataengine.apis;

import java.util.concurrent.CompletableFuture;

public interface DepJobService_I {

  CompletableFuture<Boolean> addJob(JobDTO job);
  
  CompletableFuture<Boolean> addDepJob(JobDTO job, String[] inJobIds);

  void handleJobCompleted(String jobId);

  void handleJobFailed(String jobId);

}
