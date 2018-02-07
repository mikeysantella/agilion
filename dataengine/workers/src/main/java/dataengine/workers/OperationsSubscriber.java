package dataengine.workers;

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
import dataengine.api.Operation;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.KryoSerDe;

@Slf4j
public class OperationsSubscriber implements Closeable {
  private Session session;
  private MessageConsumer consumer;
  private MessageProducer producer;
  private final Worker_I[] workers;

  public OperationsSubscriber(Connection connection, Worker_I... workers) throws JMSException {
    this.workers = workers;
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    producer = MQClient.createGenericMsgResponder(session, DeliveryMode.NON_PERSISTENT);
    listen(CommunicationConsts.OPSREGISTRY_SUBSCRIBER_TOPIC);
  }

  private void listen(String topicName) {
    KryoSerDe serde = new KryoSerDe(session);
    try {
      MQClient.createTopicConsumer(session, topicName, message -> {
          String command = message.getStringProperty(OperationsRegistry_I.COMMAND_PARAM);
          switch (OPERATIONS_REG_API.valueOf(command)) {
            case GET_OPERATIONS: {
              ArrayList<Operation> list = new ArrayList<>();
              for (Worker_I worker : workers)
                list.add(worker.operation());
              BytesMessage response = serde.writeObject(list);
              response.setJMSCorrelationID(message.getJMSCorrelationID());
              log.info("Sending response: {}", response);
              producer.send(message.getJMSReplyTo(), response);
              break;
            }
            default:
              log.warn("Unknown request: {}", message);
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
