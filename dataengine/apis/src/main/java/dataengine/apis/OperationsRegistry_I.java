package dataengine.apis;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import dataengine.api.Operation;

public interface OperationsRegistry_I {

  CompletableFuture<List<Operation>> listOperations();

}
