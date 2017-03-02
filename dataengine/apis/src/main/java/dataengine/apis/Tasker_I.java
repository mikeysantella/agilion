package dataengine.apis;

import java.util.concurrent.CompletableFuture;

import dataengine.api.Job;
import dataengine.api.Request;

public interface Tasker_I {

  /**
   * called by REST server typically when OperationsRegistry are refreshed
   */
  CompletableFuture<Void> refreshJobsCreators();
  
  /**
   * called by REST server
   * @param req
   * @return
   */
  CompletableFuture<Request> submitRequest(Request req);

//  CompletableFuture<Collection<Operation>> getJobCreatorsOperations();
  

  /**
   * called by job performer or other client
   * @param job
   * @param inputJobIds
   * @return
   */
  CompletableFuture<Boolean> addJob(Job job, String[] inputJobIds);
}
