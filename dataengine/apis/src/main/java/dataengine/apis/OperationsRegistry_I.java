package dataengine.apis;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import dataengine.api.Operation;

public interface OperationsRegistry_I {

  enum OPERATIONS_REG_API {
    QUERY_OPS
  }

  CompletableFuture<List<Operation>> listOperations();

}
