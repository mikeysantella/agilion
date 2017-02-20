package dataengine.apis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dataengine.api.Operation;

public interface OperationsRegistry_I {

  enum OPERATIONS_REG_API {
    QUERY_OPS
  }

  Class<?>[] msgBodyClasses={ Operation.class, ArrayList.class, List.class };
  
  CompletableFuture<Collection<Operation>> listOperations();

}
