package dataengine.tasker;

import javax.inject.Singleton;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.VerticleConsts;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.ActiveMqRpcServer;

@RequiredArgsConstructor
@Slf4j
final class OperationsRegistryModule extends AbstractModule {
  final Connection connection;

  @Override
  protected void configure() {
    requireBinding(Vertx.class);

    // See
    // http://stackoverflow.com/questions/14781471/guice-differences-between-singleton-class-and-singleton

    // OperationsRegistry_I used by clients
    // OperationsRegistryRpcService uses OperationsRegistryVerticle
    bind(OperationsRegistry_I.class).to(OperationsRegistryRpcService.class);
    bind(OperationsRegistryRpcService.class).in(Singleton.class);

    // OperationsRegistryVerticle to which operations are registered by providers (ie, Workers)
    if (false) {
      OperationsRegistryVerticle opsRegVert =
          new OperationsRegistryVerticle(VerticleConsts.opsRegBroadcastAddr);
      bind(OperationsRegistryVerticle.class).toInstance(opsRegVert);
    } else {
      try {
        // OperationsRegistry to which operations are registered by providers (ie, Workers)
        OperationsRegistry opsReg =
            new OperationsRegistry(connection, VerticleConsts.opsRegBroadcastAddr);
        bind(OperationsRegistry.class).toInstance(opsReg);
      } catch (JMSException e) {
        throw new RuntimeException(e);
      }
    }
  }

  static void deployOperationsRegistry(Injector injector) {
    Vertx vertx = injector.getInstance(Vertx.class);
    Connection connection = injector.getInstance(Connection.class);

    if (false) {
      OperationsRegistryVerticle opsRegVert =
          injector.getInstance(OperationsRegistryVerticle.class);
      log.info("VERTX: TASKER: Deploying OperationsRegistryVerticle: {} ", opsRegVert);
      vertx.deployVerticle(opsRegVert);
    }

    OperationsRegistry_I opsRegSvc = injector.getInstance(OperationsRegistry_I.class);
    log.info("AMQ: TASKER: Deploying RPC service for OperationsRegistry_I: {} ", opsRegSvc);
    // new RpcVerticleServer(vertx, VerticleConsts.opsRegBroadcastAddr)
    // .start("OperationsRegServiceBusAddr", opsRegSvc);
    new ActiveMqRpcServer(connection).start(VerticleConsts.opsRegBroadcastAddr, opsRegSvc);
  }
}
