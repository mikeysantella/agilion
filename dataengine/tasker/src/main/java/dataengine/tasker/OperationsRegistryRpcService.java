package dataengine.tasker;

import static java.util.stream.Collectors.toMap;

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

  final OperationsRegistry opsRegistry;
  
  @Override
  public CompletableFuture<Void> refresh() {
    log.info("SERV: refresh operations");
    return opsRegistry.refresh();
  }
  
  @Override
  public CompletableFuture<Map<String, Operation>> listOperations() {
    log.debug("SERV: listOperations()");
    return listOperations(0); 
  }

  public CompletableFuture<Map<String, Operation>> listOperations(int level) {
    log.debug("SERV: listOperations: {}", level);
    Map<String, Operation> majorOperations = opsRegistry.getOperations().entrySet().stream()
        .filter(entry->entry.getValue().getLevel()==level).collect(toMap(e->e.getKey(), e->e.getValue()));
    return CompletableFuture.completedFuture(
        // Can't find Kryo deserializer for Map.values(), so convert to basic List
        //new ArrayList<>(opsRegVert.getOperations().values())
        majorOperations
        );
  }
  
  @Override
  public CompletableFuture<Map<String, Operation>> listAllOperations() {
    log.debug("SERV: listAllOperations()");
    return CompletableFuture.completedFuture(
        // Can't find Kryo deserializer for Map.values(), so convert to basic List
        //new ArrayList<>(opsRegVert.getOperations().values())
        opsRegistry.getOperations()
        ); 
  }
}
