package dataengine.tasker;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import dataengine.api.Operation;
import dataengine.apis.OperationsRegistry_I;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.BroadcastingRegistryVerticle;

/**
 * Used by OperationsSubscriberVerticle to register themselves
 * and allow themselves to be queried by this verticle. 
 */
@Slf4j
public class OperationsRegistryVerticle extends BroadcastingRegistryVerticle {

  @Getter
  private Map<String, Operation> operations = new ConcurrentHashMap<>();

  public OperationsRegistryVerticle(String serviceType) {
    super(serviceType);
  }

  @Override
  public void start() throws Exception {
    super.start();
    registerMsgBeans(OperationsRegistry_I.msgBodyClasses);
  }

  private OperationsMerger merger = new OperationsMerger();
  
  public CompletableFuture<Map<String, Operation>> queryOperations() {
    log.info("queryOperations");
    CompletableFuture<Set<Collection<Operation>>> opsReplySet = query(QUERY_OPS.name(), null);
    return opsReplySet.thenApply(set -> {
      set.forEach(list -> list.forEach(newOp -> {
        operations.put(newOp.getId(), 
            merger.mergeOperation(newOp, operations.get(newOp.getId())));
      }));
      return operations;
    });
  }
  
  ///

  public CompletableFuture<Void> refresh() {
    operations.clear();
    merger.clear();
    return CompletableFuture.allOf(
        queryOperations()
    // TODO: 4: query other things from subscribers
    );
  }

  // TODO: 9: not sure when this would be called
  public void refreshSubscribers(long timeToWaitForSubscribers) {
    invalidateAndRebroadcast();
    try {
      Thread.sleep(timeToWaitForSubscribers); // no way to determine num of subscribers to wait for
    } catch (InterruptedException e) {
      log.warn("interrupted while waiting for subscribers", e);
    }
  }

}
