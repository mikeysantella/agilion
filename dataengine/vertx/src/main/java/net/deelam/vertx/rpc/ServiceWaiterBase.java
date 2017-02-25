package net.deelam.vertx.rpc;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
@RequiredArgsConstructor
public class ServiceWaiterBase {
  final Vertx vertx;
  final String serversBroadcastAddr;
  
  /**
   * Creates msg consumer assuming reply is a serverAddr string.
   */
  public String createMsgConsumer(CompletableFuture<String> serverAddrF) {
    return createServerMsgConsumer((String msgBody) -> {
      if (serverAddrF.isDone())
        log.warn("Already got serverAddr, ignoring this one: {}", msgBody);
      else {
        log.info("Got serverAddr={}", msgBody);
        serverAddrF.complete(msgBody);
      }
    });
  }

  private MessageConsumer<?> consumer;
  private String myAddress = UUID.randomUUID().toString();

  /**
   * Creates msg consumer with given registerServer
   * @param registerServer
   */
  public <T> String createServerMsgConsumer(Consumer<T> registerServer) {
    if (consumer != null)
      consumer.unregister();
    consumer = vertx.eventBus().consumer(myAddress, (Message<T> msg) -> {
      log.info("Got response from server: {}", msg.body());
      registerServer.accept(msg.body());
    });
    return myAddress;
  }

  public void unregisterMsgConsumer() {
    consumer.unregister();
    consumer = null;
  }

  public void broadcastServerSearch(CompletableFuture<String> serverAddrF) {
    if (consumer == null)
      throw new IllegalStateException("Must createServerMsgConsumer() before broadcastServerSearch()");
    Handler<Long> broadcastUntilSuccess =
        createBroadcastUntilSuccess(serversBroadcastAddr, myAddress, serverAddrF);
    broadcastUntilSuccess.handle(0L);
  }

  @Setter
  private long broadcastPeriodInSeconds = 3;

  private Handler<Long> createBroadcastUntilSuccess(String serversBroadcastAddr, final String myAddr,
      CompletableFuture<String> serverAddrF) {
    return (time) -> {
      if (!serverAddrF.isDone()) {
        log.info("broadcastServerSearch from={} to addr={};  waiting for server response ...", myAddr,
            serversBroadcastAddr);
        vertx.eventBus().publish(serversBroadcastAddr, myAddr);
        // check again later
        vertx.setTimer(broadcastPeriodInSeconds * 1000,
            createBroadcastUntilSuccess(serversBroadcastAddr, myAddr, serverAddrF));
      }
    };
  }
}
