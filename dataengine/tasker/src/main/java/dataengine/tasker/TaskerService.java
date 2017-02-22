package dataengine.tasker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Inject;

import dataengine.api.Progress;
import dataengine.api.Request;
import dataengine.api.State;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor=@__(@Inject))
public class TaskerService implements Tasker_I {

  final Supplier<SessionsDB_I> sessionsDBF;
  final OperationsRegistryVerticle opsRegVert;
  
  @Getter(lazy=true)
  private final SessionsDB_I sessionsDB=lazyCreateSessionsDbClient();
  
  SessionsDB_I lazyCreateSessionsDbClient(){
    log.info("-- initializing TaskerService instance");
    return sessionsDBF.get();    
  }

  @Override
  public CompletableFuture<Request> submitRequest(Request req) {
    log.debug("submitRequest: {}", req);
    CompletableFuture<Request> f = getSessionsDB().addRequest(req).thenApply((Request addedReq)->{
      // TODO: 2: add job(s) to request and submit job(s)
      log.warn("TODO: add job(s) to request and submit job(s) for workers");
      return addedReq;
    });
    return f;
  }

  @Override
  public void updateJobState(String jobId, State state) {
    // placeholder to do any checking
    getSessionsDB().updateJobState(jobId, state);
  }

  @Override
  public void updateJobProgress(String jobId, Progress progress) {
    // placeholder to do any checking
    getSessionsDB().updateJobProgress(jobId, progress);
  }

}
