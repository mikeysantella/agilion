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

  CompletableFuture<Session> setMetadata(String id, Map<String, Object> props);
  
  /// Request

  CompletableFuture<Request> addRequest(Request req);

  CompletableFuture<Boolean> hasRequest(String id);

  CompletableFuture<Request> getRequest(String id);
  
  /// Job

  CompletableFuture<Job> addJob(Job job);
  
  CompletableFuture<Job> addJob(Job job, String[] inputJobIds);

  CompletableFuture<Boolean> addJobDependency(String jobId, String inputJobId);
  
  CompletableFuture<Job> getJob(String id);

  CompletableFuture<List<Job>> getInputJobs(String jobId);
  
  CompletableFuture<List<Job>> getOutputJobs(String jobId);
  
  void updateJobState(String jobId, State state);

  void updateJobProgress(String jobId, Progress progress);

  CompletableFuture<Dataset> addOutputDataset(Dataset ds, String jobId);

  CompletableFuture<Dataset> addInputDataset(Dataset ds, String jobId);

  /// Dataset

  CompletableFuture<Dataset> getDataset(String id);




}
