package dataengine.tasker;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import dataengine.api.Operation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.BroadcastingRegistryVerticle;

@Slf4j
public class OperationsRegistryVerticle extends BroadcastingRegistryVerticle {

  @Getter
  private Collection<Operation> operations = new HashSet<>();

  public OperationsRegistryVerticle(String serviceType) {
    super(serviceType);
  }

  @Override
  public void start() throws Exception {
    super.start();
  }
  
  public void queryOperations() {
    CompletableFuture<Set<List<Operation>>> opsReplySet = query(QUERY_OPS, null);
    opsReplySet.thenAccept(set -> set.forEach(list -> operations.addAll(list)));
  }

  public void refresh() {
    operations.clear();
    queryOperations();
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
