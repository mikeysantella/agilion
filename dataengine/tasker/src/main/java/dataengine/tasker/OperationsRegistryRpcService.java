package dataengine.tasker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dataengine.api.Operation;
import dataengine.apis.OperationsRegistry_I;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC service to access the information in OperationsRegistryVerticle
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class OperationsRegistryRpcService implements OperationsRegistry_I {

  final OperationsRegistryVerticle opsRegVert;
  
  @Override
  public CompletableFuture<Void> refresh() {
    log.info("refresh()");
    return opsRegVert.refresh();
  }
  
  @Override
  public CompletableFuture<Collection<Operation>> listOperations() {
    log.info("listOperations()");
    return CompletableFuture.completedFuture(
        // Can't find Kryo deserializer for Map.values(), so convert to basic List
        new ArrayList<>(opsRegVert.getOperations().values()) 
        ); 
  }

}
