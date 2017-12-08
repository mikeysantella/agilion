package dataengine.tasker;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.GET_OPERATIONS;
import static java.util.stream.Collectors.toMap;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import dataengine.api.Operation;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.OperationsMerger;
import dataengine.apis.OperationsRegistry_I;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.CombinedResponse;
import net.deelam.activemq.TopicUtils;

/**
 * Used by OperationsSubscriber allow themselves to be queried by this class.
 * 
 */
@Slf4j
public class OperationsRegistry implements OperationsRegistry_I {

  @Getter
  private final Map<String, Operation> operations = new ConcurrentHashMap<>();

  private final Session session;
  private final TopicUtils topicUtils;

  @Inject
  public OperationsRegistry(Connection connection) throws JMSException {
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    topicUtils = new TopicUtils(session, session.createTopic(CommunicationConsts.OPSREGISTRY_SUBSCRIBER_TOPIC));
    topicUtils.listenToTopicConsumerResponses(GET_OPERATIONS.name(), null);
  }
  
  public void shutdown() throws IOException {
    topicUtils.close();
  }
  
  CompletableFuture<Map<String, Operation>> queryOperations() {
    log.info("queryOperations");
    try {
      Message message = session.createTextMessage();
      message.setStringProperty(OperationsRegistry_I.COMMAND_PARAM, GET_OPERATIONS.name());
      CombinedResponse<Collection<Operation>> cResp =
          topicUtils.queryAsync(GET_OPERATIONS.name(), message);
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

  @Override
  public CompletableFuture<Void> refresh() {
    log.info("SERV: refresh operations");
    operations.clear();
    merger.clear();
    return CompletableFuture.allOf(queryOperations()
    // TODO: 4: query other things from subscribers
    );
  }
  
  @Override
  public CompletableFuture<Map<String, Operation>> listOperations() {
    log.debug("SERV: listOperations()");
    return listOperations(0); 
  }

  public CompletableFuture<Map<String, Operation>> listOperations(int level) {
    log.debug("SERV: listOperations: {}", level);
    Map<String, Operation> majorOperations = getOperations().entrySet().stream()
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
        getOperations()
        ); 
  }
}
