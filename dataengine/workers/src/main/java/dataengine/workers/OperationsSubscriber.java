package dataengine.workers;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import dataengine.api.Operation;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.KryoSerDe;

@Slf4j
public class OperationsSubscriber implements MessageListener, Closeable {
  private final Connection connection;
  private Session session;
  private MessageConsumer consumer;
  private MessageProducer producer;
  private final Worker_I[] workers;
  private KryoSerDe serde;

  public OperationsSubscriber(Connection connection, String serviceType, Worker_I... workers) {
    this.connection = connection;
    this.workers = workers;
    listen(serviceType + QUERY_OPS.name());
  }

  private void listen(String topicName) {
    try {
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      serde = new KryoSerDe(session);
      Topic topic = session.createTopic(topicName);
      consumer = session.createConsumer(topic);
      consumer.setMessageListener(this);

      producer = session.createProducer(null);
      // Setup a message producer to respond to messages from clients, we will get the destination
      // to send to from the JMSReplyTo header field from a Message
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    } catch (Exception e) {
      log.error("When setting up OperationsSubscriber:", e);
    }
  }

  public void onMessage(Message message) {
    try {
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
    } catch (JMSException e) {
      log.error("When handling msg: {}", e);
    }
  }

  @Override
  public void close() throws IOException {
    try {
      producer.close();
      consumer.close();
      session.close();
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }
  
}
