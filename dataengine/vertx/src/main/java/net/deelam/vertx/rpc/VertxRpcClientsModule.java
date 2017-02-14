package net.deelam.vertx.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.inject.AbstractModule;

import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/// provides verticle clients used by REST services
@RequiredArgsConstructor
@Slf4j
public class VertxRpcClientsModule extends AbstractModule {
  final CompletableFuture<Vertx> vertxF;
  Vertx vertx;

  @Override
  protected void configure() {
    try {
      if(!vertxF.isDone())
        log.info("Waiting for Vertx ...");
      vertx = vertxF.get(60, TimeUnit.SECONDS);
      log.info("Got Vertx");
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  protected boolean debug = false;
  // this needs to run in a separate thread so incoming message is not blocked
  protected <T> T getClientFor(Class<T> clazz, String serviceType) {
    log.info("Creating RPC client for {}", clazz.getSimpleName());
    T rpcClient = new RpcVerticleClient(vertx, serviceType).start()
        .createRpcClient(clazz, debug); // blocks
    log.info("  Created RPC client for {}", clazz.getSimpleName());
    return rpcClient;
  }
}
