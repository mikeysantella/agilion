package dataengine.tasker;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dataengine.api.Operation;
import dataengine.apis.OperationsRegistry_I;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class OperationsRegistryService implements OperationsRegistry_I {

  final OperationsRegistryVerticle opsRegVert;
  
  @Override
  public CompletableFuture<Collection<Operation>> listOperations() {
    if(true){ // TODO: TEMPORARY: refresh every time
      return opsRegVert.refresh(); 
    } else 
    return CompletableFuture.completedFuture(opsRegVert.getOperations());
  }

}
