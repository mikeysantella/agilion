package dataengine.tasker;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dataengine.api.Operation;
import dataengine.apis.OperationsRegistry_I;
import lombok.RequiredArgsConstructor;

/**
 * RPC service to access the information in OperationsRegistryVerticle
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class OperationsRegistryRpcService implements OperationsRegistry_I {

  final OperationsRegistryVerticle opsRegVert;
  
  @Override
  public CompletableFuture<Void> refresh() {
    return opsRegVert.refresh();
  }
  
  @Override
  public CompletableFuture<Collection<Operation>> listOperations() {
    return CompletableFuture.completedFuture(opsRegVert.getOperations());
  }

}
