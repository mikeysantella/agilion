package dataengine.apis;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

// for JobConsumers
public interface JobBoardOutput_I {
  String JOBTYPE_PARAM = "jobType";

  CompletableFuture<JobListDTO> findJobs(Map<String, Object> searchParams);

  CompletableFuture<Boolean> pickedJob(String workerAddr, String jobId, long timeOfJobListQuery);

  CompletableFuture<Void> jobDone(String workerAddr, String jobId);

  CompletableFuture<Void> jobPartlyDone(String workerAddr, String jobId);// worker completed its part of the job

  CompletableFuture<Void> jobFailed(String workerAddr, String jobId);
}
