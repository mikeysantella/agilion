package dataengine.sessions;

import java.util.concurrent.CompletableFuture;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.apis.SessionsDB_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.ClusteredVertxInjectionModule;
import net.deelam.vertx.rpc.RpcVerticleServer;

@Slf4j
public class SessionsMain {
  
  public static void main(String[] args) {
    System.out.println("Starting ...");
    log.info("Starting {}", SessionsMain.class);
    Injector injector = createInjector(new CompletableFuture<>());
    SessionsDB_I sessVert = injector.getInstance(SessionsDB_I.class);
    Vertx vertx = injector.getInstance(Vertx.class);
    new RpcVerticleServer(vertx, VerticleConsts.sessionDbBroadcastAddr)
        .start("SessionsDBServiceBusAddr", sessVert);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    ClusteredVertxConfig vertxConfig=new ClusteredVertxConfig();
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF, vertxConfig),
        new TinkerGraphSessionsDbModule());
  }
}
