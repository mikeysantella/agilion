package dataengine.apis;

import java.util.concurrent.CompletableFuture;

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
  
}
