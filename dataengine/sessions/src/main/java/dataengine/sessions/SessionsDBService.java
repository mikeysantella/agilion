package dataengine.sessions;

import java.io.File;
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
import dataengine.sessions.frames.SessionFrame;
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
    log.debug("createSession: {}", session.getId());
    session.id(useOrGenerateId(session.getId()));
    String baseDir = genSessionDir(session);
    sessDB.addSessionNode(session, baseDir);
    return getSession(session.getId());
  }

  @Override
  public CompletableFuture<Request> addRequest(Request req) {
    log.debug("addRequest: {}", req.getId());
    req.id(useOrGenerateId(req.getId()));
    sessDB.addRequestNode(req);
    return getRequest(req.getId());
  }

  @Override
  public CompletableFuture<Job> addJob(Job job) {
    log.debug("addJob: {}", job.getId());
    job.id(useOrGenerateId(job.getId()));
    sessDB.addJobNode(job);
    return getJob(job.getId());
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
    log.debug("addDataset: {}", ds.getId());
    ds.id(useOrGenerateId(ds.getId()));
    sessDB.addDatasetNode(ds, jobId, io);
    return getDataset(ds.getId());
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
  public CompletableFuture<Session> setMetadata(String sId, Map<String, Object> props) {
    log.debug("setMetadata: {}", sId);
    sessDB.modifySessionNode(sId, props);
    return getSession(sId);
  }

  @Override
  public CompletableFuture<List<Session>> listSessions() {
    List<SessionFrame> list = sessDB.listSessionFrames();
    List<Session> sessList = list.stream()
        .map(sf -> SessionDB_SessionHelper.toSession(sf))
        .collect(Collectors.toList());
    return CompletableFuture.completedFuture(sessList);
  }

  @Override
  public CompletableFuture<List<String>> listSessionIds() {
    List<SessionFrame> list = sessDB.listSessionFrames();
    List<String> sessList = list.stream().map(sf -> sf.getNodeId()).collect(Collectors.toList());
    return CompletableFuture.completedFuture(sessList);
  }

  private final static boolean SAFE = false;

  @Override
  public CompletableFuture<Map<String, String>> listSessionNames() {
    if (SAFE) { // less efficient
      List<SessionFrame> list = sessDB.listSessionFrames();
      Map<String, String> sessList = list.stream()
          .collect(Collectors.toMap(sf -> sf.getLabel(), sf -> sf.getNodeId()));
      return CompletableFuture.completedFuture(sessList);
    } else { // faster
      Map<String, String> sessList = sessDB.getSessionIdsWithProperty("label");
      return CompletableFuture.completedFuture(sessList);
    }
  }

  @Override
  public CompletableFuture<Boolean> hasSession(String id) {
    log.trace("hasSession: {}", id);
    try {
      sessDB.getSessionFrame(id);
      return CompletableFuture.completedFuture(true);
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(false);
    }
  }

  @Override
  public CompletableFuture<Boolean> hasRequest(String id) {
    log.trace("hasRequest: {}", id);
    try {
      sessDB.getRequestFrame(id);
      return CompletableFuture.completedFuture(true);
    } catch (IllegalArgumentException e) {
      return CompletableFuture.completedFuture(false);
    }
  }

  @Override
  public CompletableFuture<Session> getSession(String id) {
    log.trace("getSession: {}", id);
    return CompletableFuture.completedFuture(SessionDB_SessionHelper.toSession(sessDB.getSessionFrame(id)));
  }

  @Override
  public CompletableFuture<Request> getRequest(String id) {
    log.trace("getRequest: {}", id);
    return CompletableFuture.completedFuture(SessionDB_RequestHelper.toRequest(sessDB.getRequestFrame(id)));
  }

  @Override
  public CompletableFuture<Job> getJob(String id) {
    log.trace("getJob: {}", id);
    return CompletableFuture.completedFuture(SessionDB_JobHelper.toJob(sessDB.getJobFrame(id)));
  }

  @Override
  public CompletableFuture<Dataset> getDataset(String id) {
    log.trace("getDataset: {}", id);
    return CompletableFuture.completedFuture(SessionDB_DatasetHelper.toDataset(sessDB.getDatasetFrame(id)));
  }

  @Override
  public void updateJobState(String jobId, State state) {
    log.trace("updateJobState: {} to {}", jobId, state);
    sessDB.updateJobState(jobId, state);
  }

  @Override
  public void updateJobProgress(String jobId, Progress progress) {
    log.trace("updateJobProgress: {} to {}", jobId, progress);
    sessDB.updateJobProgress(jobId, progress);
  }


}
