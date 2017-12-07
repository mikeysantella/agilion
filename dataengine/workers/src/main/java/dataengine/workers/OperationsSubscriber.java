package dataengine.workers;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import dataengine.api.Operation;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.KryoSerDe;

@Slf4j
public class OperationsSubscriber implements Closeable {
  private final Connection connection;
  private Session session;
  private MessageConsumer consumer;
  private MessageProducer producer;
  private final Worker_I[] workers;

  public OperationsSubscriber(Connection connection, String serviceType, Worker_I... workers) {
    this.connection = connection;
    this.workers = workers;
    listen(serviceType + QUERY_OPS.name());
  }

  private void listen(String topicName) {
    try {
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      KryoSerDe serde = new KryoSerDe(session);
      producer = MQClient.createGenericMsgResponder(session, DeliveryMode.NON_PERSISTENT);
      
      MQClient.createTopicConsumer(session, topicName, message -> {
        if (message instanceof TextMessage) {
          String body = ((TextMessage) message).getText();
          if (QUERY_OPS.name().equals(body)) {
            ArrayList<Operation> list = new ArrayList<>();
            for (Worker_I worker : workers)
              list.add(worker.operation());
            BytesMessage response = serde.writeObject(list);
            response.setJMSCorrelationID(message.getJMSCorrelationID());
            log.info("Sending response: {}", response);
            producer.send(message.getJMSReplyTo(), response);
          } else {
            log.warn("Unknown request: {}", body);
          }
        } else {
          log.warn("Unhandled message type: {}", message);
        }
      });

    } catch (Exception e) {
      log.error("When setting up OperationsSubscriber:", e);
    }
  }


  @Override
  public void close() throws IOException {
    try {
      producer.close();
      consumer.close();
      session.close();
    } catch (JMSException e) {
      throw new IllegalStateException("When closing "+this.getClass().getSimpleName(), e);
    }
  }
  
}
