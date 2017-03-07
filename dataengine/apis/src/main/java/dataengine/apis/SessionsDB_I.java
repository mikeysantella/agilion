package dataengine.apis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dataengine.api.Dataset;
import dataengine.api.Job;
import dataengine.api.Progress;
import dataengine.api.Request;
import dataengine.api.Session;
import dataengine.api.State;

public interface SessionsDB_I {

  /// Session

  CompletableFuture<Session> createSession(Session session);

  CompletableFuture<Boolean> hasSession(String id);

  CompletableFuture<Session> getSession(String id);

  CompletableFuture<List<Session>> listSessions();

  CompletableFuture<List<String>> listSessionIds();

  CompletableFuture<Map<String, String>> listSessionNames();

  CompletableFuture<Session> setSessionMetadata(String id, Map<String, Object> props);
  
  /// Request

  CompletableFuture<Request> addRequest(Request req);

  CompletableFuture<Boolean> hasRequest(String id);

  CompletableFuture<Request> getRequest(String id);
  
  void updateRequestState(String reqId, State state);
  
  /// Job

  CompletableFuture<Job> addJob(Job job);
  
  CompletableFuture<Job> addJob(Job job, String[] inputJobIds);

  CompletableFuture<Boolean> addJobDependency(String jobId, String inputJobId);
  
  CompletableFuture<Job> getJob(String id);

  CompletableFuture<List<Job>> getInputJobs(String jobId);
  
  CompletableFuture<List<Job>> getOutputJobs(String jobId);
  
  CompletableFuture<Void> setJobParam(String jobId, String key, Object value);
  
  void updateJobState(String jobId, State state);

  void updateJobProgress(String jobId, Progress progress);

  CompletableFuture<Dataset> addOutputDataset(Dataset ds, String jobId);

  CompletableFuture<Dataset> addInputDataset(Dataset ds, String jobId);

  CompletableFuture<String> getLastJobIdOfRequest(String requestId);
  
  /// Dataset

  CompletableFuture<Dataset> getDataset(String id);

  CompletableFuture<Void> connectAsOutputDatasetNode(String requestId, String datasetId);

}
