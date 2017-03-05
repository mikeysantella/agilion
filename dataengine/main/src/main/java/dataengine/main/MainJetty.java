package dataengine.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dataengine.server.DeServerGuiceInjector;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxInjectionModule;

/**
 * 
 * This class is used to run in Eclipse or as a Jar file: 'gradle run'.
 * By default, all needed subservices in other projects will be started in this JVM.
 * 
 * To run using Gretty ('gradle jettyRun'), use the Bootstrap class; for this, MainJetty is not used.
 * All needed subservices in other projects will be started in the same JVM.
 * 
 * To deploy as a war file, use the 'dataengine.server' project instead.
 * In which case, other projects need to be started in other JVMs.
 */
@Slf4j
public class MainJetty {
  public static void main(String[] args) throws Exception {
    String onlyRunServerProject = System.getProperty("ONLY_RUN_SERVER");
    boolean runInSingleJVM = (onlyRunServerProject == null) 
        ? true : Boolean.getBoolean(onlyRunServerProject);

    injectVertx(runInSingleJVM);

    MainJetty main = new MainJetty();
    Server jettyServer = main.startServer(8080, 8083,
        // this contextPath mimics gretty's default behavior
        "/main" // gretty uses the project name
            + "/DataEngine/0.0.2", // matches the path in web.xml
        null, null, false); // TODO: 4: enable SSL

    try {
      jettyServer.start();
      jettyServer.join();
    } finally {
      jettyServer.destroy();
    }
  }

  public static void injectVertx(boolean runInSingleJVM) {
    CompletableFuture<Vertx> vertxF = DeServerGuiceInjector.vertxF();
    if (runInSingleJVM) {
      vertxF.complete(Vertx.vertx());
    } else {
      Injector injector = Guice.createInjector(
          new ClusteredVertxInjectionModule(vertxF));
      vertxF.complete(injector.getInstance(Vertx.class));
    }
    if (runInSingleJVM) {
      vertxF.join();
      log.info("======== Running all required DataEngine services in same JVM {}", vertxF);
      try {
        startAllInSameJvm(vertxF);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void startAllInSameJvm(CompletableFuture<Vertx> vertxF) throws IOException {
    // Only create 1 Vertx instance per JVM! 
    // https://groups.google.com/forum/#!topic/vertx/sGeuSg3GxwY
    dataengine.sessions.SessionsMain.main(vertxF);
    dataengine.tasker.TaskerMain.main(vertxF);
    dataengine.jobmgr.JobManagerMain.main(vertxF);
    dataengine.workers.WorkerMain.main(vertxF);
  }

  protected Properties props = new Properties();

  private Server startServer(int port, int sslPort, String contextPath,
      String keyStoreFile, String keyStorePwd, boolean validateCerts) throws FileNotFoundException {
    final ResourceConfig rc = new MainJerseyRestResource();
    ServletContainer servletCont = new ServletContainer(rc);
    ServletHolder servletHolder = new ServletHolder(servletCont);

    Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);

    ServletContextHandler context = new ServletContextHandler(server, contextPath);
    context.addServlet(servletHolder, "/*");
    server.setHandler(context);

    if (keyStoreFile != null) {
      File keystoreFile = new File(keyStoreFile);
      if (!keystoreFile.exists())
        throw new FileNotFoundException("Cannot find " + keystoreFile.getAbsolutePath());

      String keyStorePath = keystoreFile.getAbsolutePath();
      log.info("keyStorePath={}", keyStorePath);
      if (keyStorePwd != null)
        log.info("keyStorePwd.length={}", keyStorePwd.length());

      HttpConfiguration https = new HttpConfiguration();
      https.addCustomizer(new SecureRequestCustomizer());
      SslContextFactory sslContextFactory = new SslContextFactory();
      sslContextFactory.setKeyStorePath(keyStorePath);
      if (keyStorePwd != null) {
        sslContextFactory.setKeyStorePassword(keyStorePwd);
        sslContextFactory.setKeyManagerPassword(keyStorePwd);
      }
      log.info("validateCerts={}", validateCerts);
      sslContextFactory.setValidateCerts(validateCerts);
      ServerConnector sslConnector = new ServerConnector(server,
          new SslConnectionFactory(sslContextFactory, "http/1.1"),
          new HttpConnectionFactory(https));
      sslConnector.setPort(sslPort);
      server.setConnectors(new Connector[] {connector, sslConnector});
    } else {
      server.setConnectors(new Connector[] {connector});
    }
    log.info("Starting server");
    return server;
  }
}
