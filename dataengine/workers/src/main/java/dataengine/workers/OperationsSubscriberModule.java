package dataengine.workers;

import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class OperationsSubscriberModule extends AbstractModule {
  @Override
  protected void configure() {}

  static int subscriberCounter = 0;

  public static void deployOperationsSubscriber(Connection connection, Worker_I worker)
      throws JMSException {
    OperationsSubscriber opSubscriber = new OperationsSubscriber(connection, worker);
    // TODO: call opSubscriber.close()
  }
}
