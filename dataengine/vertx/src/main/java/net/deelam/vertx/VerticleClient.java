package net.deelam.vertx;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * To be used with AbstractServerVerticle
 *
 */
@Slf4j
@ToString
public class VerticleClient {
  private final String serviceType;
  private final Vertx vertx;

  public VerticleClient(Vertx vertx, String serviceType) {
    super();
    this.serviceType = serviceType;
    this.vertx = vertx;
    start(Collections.emptySet());
  }

  public VerticleClient(Vertx vertx, String serviceType, Set<Class<?>> msgBeans) {
    super();
    this.serviceType = serviceType;
    this.vertx = vertx;
    start(msgBeans);
  }

  private CompletableFuture<String> getSvcAddrPrefix = new CompletableFuture<>();
  String myAnnounceInbox;
  protected String svcAddrPrefix;

  private synchronized void waitForSvcAddress() {
    if (!getSvcAddrPrefix.isDone()) {
      try {
        do {
          try {
            log.info("Waiting for address of server with serviceType={}", serviceType);
            getSvcAddrPrefix.get(2, TimeUnit.SECONDS);
          } catch (TimeoutException e) {
            log.info("Timed out waiting for address of server with serviceType={}; reannouncing client", serviceType);
            VerticleUtils.reannounceClientType(vertx, serviceType, myAnnounceInbox);
          }
        } while (!getSvcAddrPrefix.isDone());
      } catch (ExecutionException | InterruptedException e) {
        log.error("Could not get address for a server with serviceType=" + serviceType, e);
      }
      try {
        svcAddrPrefix = getSvcAddrPrefix.get();
        log.info("Done waiting for address of server with serviceType={}: address={}", serviceType, svcAddrPrefix);
      } catch (InterruptedException | ExecutionException e) {
        log.error("Could not get address for a server with serviceType=" + serviceType, e);
      }
    }
  }

  private void start(Set<Class<?>> msgBeans) {
    msgBeans.forEach(beanClass -> KryoMessageCodec.register(vertx.eventBus(), beanClass));

    myAnnounceInbox = VerticleUtils.announceClientType(vertx, serviceType, msg -> {
      svcAddrPrefix = msg.body();
      log.info("Got serviceAddress={}", svcAddrPrefix);
      // set svcAddrPrefix before completing getSvcAddrPrefix and unblocking getSvcAddrPrefix.get() calls
      getSvcAddrPrefix.complete(svcAddrPrefix);
    });

  }

  public <T> Future<T> notify(Enum<?> method, T msg) {
    return notify(method.name(), msg);
  }

  /**
   * Call a void-returning method on the service with arguments.
   * Can wait on response by calling Future.get() on the returned Future
   * @param method to be called on service
   * @param argsObj to be passed to method
   * @return Future containing the sent argsObj
   */
  public <T> Future<T> notify(String method, T argsObj) {
    waitForSvcAddress();
    //threadPool.execute(()->{
    log.info("Sending msg to {}: {}", svcAddrPrefix + method, argsObj);
    CompletableFuture<T> future = new CompletableFuture<>();
    vertx.eventBus().send(svcAddrPrefix + method, argsObj, (AsyncResult<Message<T>> resp) -> {
      if (resp.failed()) {
        log.error("Message failed: msg=" + argsObj, resp.cause());
        future.completeExceptionally(resp.cause());
      } else if (resp.succeeded()) {
        log.debug("send response={}", resp.result().body());
        future.complete(argsObj);
      }
    });
    return future;
  }

  public <T, R> Future<R> query(Enum<?> method, T msg) {
    return query(method.name(), msg);
  }

  /**
   * Call an object-returning method on the service with arguments.
   * Get the result object (of the method) by calling Future.get() on the returned Future
   * @param method to be called on service
   * @param argsObj primitive or JsonArray or JsonObject or Bean (remember to registerMessageBeans()) to be passed to method
   * @return a Future containing a primitive or JsonArray or JsonObject
   */
  public <T, R> Future<R> query(String method, T argsObj) {
    waitForSvcAddress();
    CompletableFuture<R> future = new CompletableFuture<>();
    log.info("Sending query to {}: {}", svcAddrPrefix + method, argsObj);
    vertx.eventBus().send(svcAddrPrefix + method, argsObj, (AsyncResult<Message<R>> resp) -> {
      if (resp.failed()) {
        log.error("Message failed: msg=" + argsObj, resp.cause());
        future.completeExceptionally(resp.cause());
      } else {
        log.debug("query response={}", resp.result().body());
        future.complete(resp.result().body());
      }
    });
    return future;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] jsonArrayToObjectArray(JsonArray ja, T[] destArray) {
    return (T[]) ja.getList().toArray(destArray);
  }

}
