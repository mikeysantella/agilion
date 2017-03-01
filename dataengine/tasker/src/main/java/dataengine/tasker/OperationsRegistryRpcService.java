package dataengine.tasker;

import java.util.Map;
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
    log.info("SERV: refresh()");
    return opsRegVert.refresh();
  }
  
  @Override
  public CompletableFuture<Map<String, Operation>> listOperations() {
    log.info("SERV: listOperations()");
    return CompletableFuture.completedFuture(
        // Can't find Kryo deserializer for Map.values(), so convert to basic List
        //new ArrayList<>(opsRegVert.getOperations().values())
        opsRegVert.getOperations()
        ); 
  }

}
