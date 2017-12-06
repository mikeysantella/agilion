package net.deelam.vertx.jobboard;

import java.util.concurrent.CompletableFuture;
import dataengine.apis.JobDTO;

// for JobProducers
public interface JobBoardInput_I {
  CompletableFuture<Boolean> addJob(JobDTO job, int maxRetries);
  CompletableFuture<Boolean> removeJob(String jobId);
}
