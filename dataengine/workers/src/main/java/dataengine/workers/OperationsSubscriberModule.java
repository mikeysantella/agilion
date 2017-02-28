package dataengine.workers;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class OperationsSubscriberModule extends AbstractModule {
  @Override
  protected void configure() {}

  static int subscriberCounter = 0;

  public static void deployOperationsSubscriberVerticle(Injector injector, Worker_I worker) {
    Vertx vertx = injector.getInstance(Vertx.class);
    OperationsSubscriberVerticle opsRegVert =
        new OperationsSubscriberVerticle(VerticleConsts.opsRegBroadcastAddr,
            "ops" + (++subscriberCounter) + "-" + worker.getName() + System.currentTimeMillis(),
            worker);
    vertx.deployVerticle(opsRegVert);
    log.info("Created OperationsSubscriberVerticle: {} for {}", opsRegVert, worker);
  }
}
