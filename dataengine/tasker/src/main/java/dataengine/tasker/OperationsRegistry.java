package dataengine.tasker;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import dataengine.api.Operation;
import dataengine.apis.OperationsMerger;
import dataengine.apis.OperationsRegistry_I;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.CombinedResponse;
import net.deelam.activemq.TopicUtils;

/**
 * Used by OperationsSubscriberVerticle to register themselves and allow themselves to be queried by
 * this class.
 * 
 * OperationsRegistryRpcService offers this verticle as an RPC service
 */
@Slf4j
public class OperationsRegistry {

  @Getter
  private Map<String, Operation> operations = new ConcurrentHashMap<>();

  private Session session;
  private TopicUtils topicUtils;

  public OperationsRegistry(Connection connection, String serviceType) throws JMSException {
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    topicUtils = new TopicUtils(session, session.createTopic(serviceType+QUERY_OPS.name()));
    topicUtils.listenToTopicConsumerResponses(QUERY_OPS.name(), null);
  }
  
  CompletableFuture<Map<String, Operation>> queryOperations() {
    log.info("queryOperations");
    try {
      Message message = session.createTextMessage(QUERY_OPS.name());
      CombinedResponse<Collection<Operation>> cResp =
          topicUtils.queryAsync(QUERY_OPS.name(), message);
      return cResp.getFuture().thenApply(replies -> {
        replies.forEach(list -> list.forEach(this::mergeOperation));
        return operations;
      });
    } catch (JMSException e) {
      log.error("When queryOperations: ", e);
      return CompletableFuture.completedFuture(Collections.emptyMap());
    }
  }

  private OperationsMerger merger = new OperationsMerger();

  void mergeOperation(Operation newOp) {
    operations.put(newOp.getId(), merger.mergeOperation(newOp, operations.get(newOp.getId())));
  }

  ///

  public CompletableFuture<Void> refresh() {
    operations.clear();
    merger.clear();
    return CompletableFuture.allOf(queryOperations()
    // TODO: 4: query other things from subscribers
    );
  }

}
