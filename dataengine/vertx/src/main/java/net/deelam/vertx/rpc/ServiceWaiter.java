package net.deelam.vertx.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.vertx.core.Vertx;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
public class ServiceWaiter {

  final ServiceWaiterBase base;

  public ServiceWaiter(Vertx vertx, String serversBroadcastAddr) {
    base = new ServiceWaiterBase(vertx, serversBroadcastAddr);
  }

  public CompletableFuture<String> listenAndBroadCast() {
    serverAddrF = new CompletableFuture<>();
    base.createMsgConsumer(serverAddrF);
    base.broadcastServerSearch(serverAddrF);
    return serverAddrF;
  }

  CompletableFuture<String> serverAddrF;

  public String awaitServiceAddress() {
    if (serverAddrF == null)
      throw new IllegalStateException("Call listenAndBroadCast() first!");
    while (true)
      try {
        if(!serverAddrF.isDone())
          log.info("Waiting for server response ...");
        String serverAddr = serverAddrF.get(); // wait for serverAddr
        //log.debug("Got server address: {}", serverAddr);
        return serverAddr;
      } catch (InterruptedException | ExecutionException e) {
        log.warn(" Retrying to get serverAddr", e);
      }
  }

}
