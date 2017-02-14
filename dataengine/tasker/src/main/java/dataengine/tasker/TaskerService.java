package dataengine.tasker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dataengine.api.Progress;
import dataengine.api.Request;
import dataengine.api.State;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor=@__(@Inject))
public class TaskerService implements Tasker_I {

  final SessionsDB_I sessionsDB;
  {
    log.info("-- TaskerService instance created");
  }

  @Override
  public CompletableFuture<Request> submitRequest(Request req) {
    log.debug("submitRequest: {}", req);
    CompletableFuture<Request> f = sessionsDB.addRequest(req).thenApply((Request addedReq)->{
      // TODO: 2: add job(s) to request and submit job(s)
      log.warn("TODO: add job(s) to request and submit job(s) for workers");
      return addedReq;
    });
    return f;
  }

  @Override
  public void updateJobState(String jobId, State state) {
    // TODO Auto-generated method stub
  }

  @Override
  public void updateJobProgress(String jobId, Progress progress) {
    // TODO Auto-generated method stub
  }

}
