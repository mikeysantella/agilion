package dataengine.apis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dataengine.api.Operation;

public interface OperationsRegistry_I {

  enum OPERATIONS_REG_API {
    QUERY_OPS
  }

  Class<?>[] msgBodyClasses={ Operation.class, ArrayList.class, List.class };
  
  CompletableFuture<Void> refresh();

  CompletableFuture<Map<String, Operation>> listOperations();

  CompletableFuture<Map<String, Operation>> listAllOperations();

}
