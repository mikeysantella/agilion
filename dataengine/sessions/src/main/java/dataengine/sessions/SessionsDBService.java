package dataengine.sessions;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dataengine.api.Dataset;
import dataengine.api.Job;
import dataengine.api.Progress;
import dataengine.api.Request;
import dataengine.api.Session;
import dataengine.api.State;
import dataengine.apis.SessionsDB_I;
import dataengine.sessions.SessionDB_DatasetHelper.IO;
import dataengine.sessions.frames.JobFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class SessionsDBService implements SessionsDB_I {
  private final SessionDB sessDB;

  {
    log.info("-- SessionsDBService instance created");
  }

  @Override
  public CompletableFuture<Session> createSession(Session session) {
    log.info("SERV: createSession: {}", session.getId());
    session.id(useOrGenerateId(session.getId()));
    String baseDir = genSessionDir(session);
    sessDB.addSessionNode(session, baseDir);
    return getSession(session.getId());
  }

  @Override
  public CompletableFuture<Request> addRequest(Request req) {
    log.info("SERV: addRequest: {}", req.getId());
    req.id(useOrGenerateId(req.getId()));
    sessDB.addRequestNode(req);
    return getRequest(req.getId());
  }

  @Override
  public CompletableFuture<Job> addJob(Job job) {
    return addJob(job, null);
  }

  @Override
  public CompletableFuture<Job> addJob(Job job, String[] inputJobIds) {
    log.info("SERV: addJob: {} {}", job.getId(), Arrays.toString(inputJobIds));
    job.id(useOrGenerateId(job.getId()));
    sessDB.addJobNode(job, inputJobIds);
    return getJob(job.getId());
  }

  @Override
  public CompletableFuture<Boolean> addJobDependency(String jobId, String inputJobId) {
    log.info("SERV: addJobDependency: {} {}", jobId, inputJobId);
    sessDB.addJobDependency(jobId, inputJobId);
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public CompletableFuture<List<Job>> getInputJobs(String jobId) {
    log.info("SERV: getInputJobs: {}", jobId);
    List<JobFrame> jobs = sessDB.getInputJobs(jobId);
    return CompletableFuture.completedFuture(jobs.stream().map(SessionDB_JobHelper::toJob).collect(toList()));
  }

  @Override
  public CompletableFuture<List<Job>> getOutputJobs(String jobId) {
    log.info("SERV: getOutputJobs: {}", jobId);
    List<JobFrame> jobs = sessDB.getOutputJobs(jobId);
    return CompletableFuture.completedFuture(jobs.stream().map(SessionDB_JobHelper::toJob).collect(toList()));
  }
  
  @Override
  public CompletableFuture<Dataset> addOutputDataset(Dataset ds, String jobId) {
    return addDatasetToJob(ds, jobId, IO.OUTPUT);
  }

  @Override
  public CompletableFuture<Dataset> addInputDataset(Dataset ds, String jobId) {
    return addDatasetToJob(ds, jobId, IO.INPUT);
  }

  private CompletableFuture<Dataset> addDatasetToJob(Dataset ds, String jobId, IO io) {
    log.info("SERV: addDatasetToJob: {}", ds.getId());
    ds.id(useOrGenerateId(ds.getId()));
    sessDB.addDatasetNode(ds, jobId, io);
    return getDataset(ds.getId());
  }

  @Override
  public CompletableFuture<Void> connectAsOutputDatasetNode(String requestId, String datasetId) {
    sessDB.connectAsOutputDatasetNode(requestId, datasetId);
    return CompletableFuture.completedFuture(null);
  }

  private String useOrGenerateId(String sId) {
    if (sId != null) {
      if (sessDB.hasNode(sId))
        throw new IllegalArgumentException("Id already exists: " + sessDB.getSessionFrame(sId));
    } else {
      sId = UUID.randomUUID().toString();
    }
    return sId;
  }

  @Getter
  private String baseDir = ".";

  private String genSessionDir(Session session) {
    return new File(baseDir, session.getId()).getAbsolutePath();
  }

  @Override
  public CompletableFuture<Session> setSessionMetadata(String sId, Map<String, Object> props) {
    log.info("SERV: setMetadata: {}", sId);
    sessDB.modifySessionNode(sId, props);
    return getSession(sId);
  }

  @Override
  public CompletableFuture<List<Session>> listSessions() {
    log.info("SERV: listSessions");
    return CompletableFuture.completedFuture(SessionDB_SessionHelper.toSessions(sessDB.listSessionFrames()));
  }

  @Override
  public CompletableFuture<List<String>> listSessionIds() {
    log.info("SERV: listSessionIds");
    List<Session> list = listSessions().join();
    List<String> sessList = list.stream().map(s -> s.getId()).collect(Collectors.toList());
    return CompletableFuture.completedFuture(sessList);
  }

  private final static boolean SAFE = false;

  @Override
  public CompletableFuture<Map<String, String>> listSessionNames() {
    log.info("SERV: listSessionNames");
    if (SAFE) { // less efficient
      List<Session> list = listSessions().join();
      Map<String, String> sessList = list.stream()
          .collect(Collectors.toMap(s -> s.getLabel(), s -> s.getId()));
      return CompletableFuture.completedFuture(sessList);
    } else { // faster
      Map<String, String> sessList = sessDB.getSessionIdsWithProperty("label");
      return CompletableFuture.completedFuture(sessList);
    }
  }

  @Override
  public CompletableFuture<Boolean> hasSession(String id) {
    log.debug("SERV: hasSession: {}", id);
    return CompletableFuture.completedFuture(sessDB.hasSession(id));
  }

  @Override
  public CompletableFuture<Boolean> hasRequest(String id) {
    log.debug("SERV: hasRequest: {}", id);
    return CompletableFuture.completedFuture(sessDB.hasRequest(id));
  }

  @Override
  public CompletableFuture<Session> getSession(String id) {
    log.debug("SERV: getSession: {}", id);
    return CompletableFuture.completedFuture(SessionDB_SessionHelper.toSession(sessDB.getSessionFrame(id)));
  }

  @Override
  public CompletableFuture<Request> getRequest(String id) {
    log.debug("SERV: getRequest: {}", id);
    return CompletableFuture.completedFuture(SessionDB_RequestHelper.toRequest(sessDB.getRequestFrame(id)));
  }

  @Override
  public CompletableFuture<Job> getJob(String id) {
    log.debug("SERV: getJob: {}", id);
    return CompletableFuture.completedFuture(SessionDB_JobHelper.toJob(sessDB.getJobFrame(id)));
  }

  @Override
  public CompletableFuture<Dataset> getDataset(String id) {
    log.debug("SERV: getDataset: {}", id);
    return CompletableFuture.completedFuture(SessionDB_DatasetHelper.toDataset(sessDB.getDatasetFrame(id)));
  }
  
  @Override
  public void setJobParam(String jobId, String key, Object value){
    log.debug("SERV: setJobParam of {}: {}={}", jobId, key, value);
    sessDB.setJobParam(jobId, key, value);;
  }

  @Override
  public void updateJobState(String jobId, State state) {
    log.debug("SERV: updateJobState: {} to {}", jobId, state);
    sessDB.updateJobState(jobId, state);
  }

  @Override
  public void updateJobProgress(String jobId, Progress progress) {
    log.debug("SERV: updateJobProgress: {} to {}", jobId, progress);
    sessDB.updateJobProgress(jobId, progress);
  }


}
