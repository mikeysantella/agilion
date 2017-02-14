package net.deelam.vertx.rpc;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 *  Use case: Client initiates call to Server and will wait until Server is available.
 *  
 *  Client will repeatedly broadcast a connect request message until 
 *  it gets response from at least one Server(s).  
 *  It keeps 1 (or all) addresses from responses to make calls.  
 *  Client has a message consumer on a unique address to listen for server responses.
 *  Server has a consumer on the broadcast address to listen for client connection requests.
 *  Server doesn't need to announce (broadcast) on startup.  
 *  Server responds to Client's broadcast with a notify, not waiting for an ACK reply (client can re-broadcast).
 *  If Server goes down (or client call fails), client broadcast for another Server and re-calls.
 *  
 *  Use vertx-rpc to make calls, catching exceptions to retry (with another server).
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
@Slf4j
public class RpcVerticleClient {
  final Vertx vertx;
  final String serversBroadcastAddr;
  final String myAddress = UUID.randomUUID().toString();

  CompletableFuture<String> serverAddrF;

  public RpcVerticleClient start() {
    serverAddrF = new CompletableFuture<>();
    createMsgConsumer(serverAddrF);
    broadcastServerSearch(serverAddrF);
    return this;
  }

  public <T> T invalidateAndFindNewServer(Class<T> clazz) {
    serverAddrF = new CompletableFuture<>();
    createMsgConsumer(serverAddrF);
    broadcastServerSearch(serverAddrF);
    return createRpcClient(clazz);
  }

  /**
   * Creates msg consumer assuming reply is a serverAddr string.
   */
  void createMsgConsumer(CompletableFuture<String> serverAddrF) {
    createServerMsgConsumer((String msgBody) -> {
      if (serverAddrF.isDone())
        log.warn("Already got serverAddr, ignoring this one: {}", msgBody);
      else {
        log.info("Got serverAddr={}", msgBody);
        serverAddrF.complete(msgBody);
      }
    });
  }

  private CompletableFuture<String> myAddressF = new CompletableFuture<>();

  private MessageConsumer<?> consumer;

  /**
   * Creates msg consumer with given registerServer
   * @param registerServer
   */
  <T> void createServerMsgConsumer(Consumer<T> registerServer) {
    if (consumer != null)
      consumer.unregister();
    consumer = vertx.eventBus().consumer(myAddress, (Message<T> msg) -> {
      log.info("Got response from server: {}", msg.body());
      registerServer.accept(msg.body());
    });
    myAddressF.complete(myAddress);
  }

  void broadcastServerSearch(CompletableFuture<String> serverAddrF) {
    if (!myAddressF.isDone())
      throw new IllegalStateException("Must createServerMsgConsumer() before broadcastServerSearch()");
    try {
      Handler<Long> broadcastUntilSuccess =
          createBroadcastUntilSuccess(serversBroadcastAddr, myAddressF.get(), serverAddrF);
      broadcastUntilSuccess.handle(0L);
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(" Should not happen", e);
    }
  }

  @Setter
  private long broadcastPeriodInSeconds = 3;

  private Handler<Long> createBroadcastUntilSuccess(String serversBroadcastAddr, final String myAddr,
      CompletableFuture<String> serverAddrF) {
    return (time) -> {
      if (!serverAddrF.isDone()) {
        log.info("broadcastServerSearch from={} to addr={};  waiting for server response ...", myAddr, serversBroadcastAddr);
        vertx.eventBus().publish(serversBroadcastAddr, myAddr);
        // check again later
        vertx.setTimer(broadcastPeriodInSeconds * 1000,
            createBroadcastUntilSuccess(serversBroadcastAddr, myAddr, serverAddrF));
      }
    };
  }

  /**
   * Creates RPC client, blocking until serverAddr received from server
   * @param clazz
   * @return proxy for server
   */
  public <T> T createRpcClient(Class<T> clazz) {
    return createRpcClient(clazz, false);
  }

  /**
   * Creates RPC client, blocking until serverAddr received from server
   * @param clazz
   * @param withDebugHook whether to add a hook that logs RPC calls
   * @return proxy for server
   */
  public <T> T createRpcClient(Class<T> clazz, boolean withDebugHook) {
    if (serverAddrF == null)
      throw new IllegalStateException("Run start() first.");

    String serverAddr = null;
    while (serverAddr == null)
      try {
        serverAddr = serverAddrF.get();
      } catch (InterruptedException | ExecutionException e) {
        log.warn(" Retrying to get serverAddr", e);
      }
    return createRpcClient(serverAddr, clazz, withDebugHook);
  }

  /**
   * Creates RPC client at serverAddr
   * @param serverAddr
   * @param clazz
   * @return proxy for server
   */
  <T> T createRpcClient(String serverAddr, Class<T> iface, boolean withDebugHook) {
    VertxRpcUtil rpc=new VertxRpcUtil(vertx.eventBus(), serverAddr);
    if (withDebugHook)
      rpc.setHook(new VertxRpcUtil.DebugRpcHook());
    return rpc.createClient(iface);
  }

}
