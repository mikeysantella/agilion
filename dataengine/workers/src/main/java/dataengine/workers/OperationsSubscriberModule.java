package dataengine.workers;

import javax.jms.Connection;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import dataengine.apis.VerticleConsts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class OperationsSubscriberModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  static int subscriberCounter = 0;

  public static void deployOperationsSubscriberVerticle(Injector injector, Connection connection,
      Worker_I worker) {
    if (false) {
    } else {
      OperationsSubscriber opSubscriber =
          new OperationsSubscriber(connection, VerticleConsts.opsRegBroadcastAddr, worker);
      //TODO: call opSubscriber.close()
    }
  }
}
