package dataengine.apis;

import java.util.concurrent.CompletableFuture;

// for JobProducers
public interface JobBoardInput_I {
  CompletableFuture<Boolean> addJob(JobDTO job, int maxRetries);
  CompletableFuture<Boolean> removeJob(String jobId);
}
