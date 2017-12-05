package dataengine.sessions;

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
public class SessionsMain {
  
  public static void main(String[] args) throws IOException {
    main(new CompletableFuture<>());
  }
  public static void main(CompletableFuture<Vertx> vertxF) throws IOException {
    System.out.println("Starting "+SessionsMain.class.getSimpleName());
    log.info("Starting {}", SessionsMain.class);
    Properties properties=new Properties();
    PropertiesUtil.loadProperties("sessions.props", properties);
    Injector injector = createInjector(vertxF);
    TinkerGraphSessionsDbModule.deploySessionDb(injector);
  }

  static Injector createInjector(CompletableFuture<Vertx> vertxF) {
    return Guice.createInjector(
        new ClusteredVertxInjectionModule(vertxF),
        new TinkerGraphSessionsDbModule());
  }
}
