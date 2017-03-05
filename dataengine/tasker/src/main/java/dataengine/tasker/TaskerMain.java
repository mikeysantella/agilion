package dataengine.tasker;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;
import net.deelam.vertx.ClusteredVertxInjectionModule;

@Slf4j
public class TaskerMain {

  public static void main(String[] args) throws IOException {
    main(new CompletableFuture<>());
  }

  public static void main(CompletableFuture<Vertx> vertxF) throws IOException {
    log.info("Starting {}", TaskerMain.class);
    Properties properties=new Properties();
    PropertiesUtil.loadProperties("tasker.props", properties);
    Injector injector = createInjector(vertxF, properties);
    
    OperationsRegistryModule.deployOperationsRegistry(injector);
    TaskerModule.deployTasker(injector);
    TaskerModule.deployJobListener(injector);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF, Properties properties) {
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF),
        new VertxRpcClients4TaskerModule(vertxF),
        new OperationsRegistryModule(),
        new TaskerModule(properties)
        );
  }

}
