package dataengine.tasker;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
  private Map<String, Operation> operations = new HashMap<>();

  public OperationsRegistryVerticle(String serviceType) {
    super(serviceType);
  }

  @Override
  public void start() throws Exception {
    super.start();
    registerMsgBeans(OperationsRegistry_I.msgBodyClasses);
  }

  public CompletableFuture<Map<String, Operation>> queryOperations() {
    CompletableFuture<Set<List<Operation>>> opsReplySet = query(QUERY_OPS.name(), null);
    return opsReplySet.thenApply(set -> {
      set.forEach(list -> list.forEach(op -> {
        Operation existing = operations.put(op.getId(), op);
        if(existing!=null)
          log.warn("Replaced existing operation with same id={}: prev={} curr={}", op.getId(), existing, op);
      }));
      return operations;
    });
  }

  public CompletableFuture<Void> refresh() {
    operations.clear();
    return CompletableFuture.allOf(
        queryOperations()
    // TODO: 4: query other things from subscribers
    );
  }

  public void refreshSubscribers(long timeToWaitForSubscribers) {
    invalidateAndRebroadcast();
    try {
      Thread.sleep(timeToWaitForSubscribers);
    } catch (InterruptedException e) {
      log.warn("interrupted while waiting for subscribers", e);
    }
  }

}
