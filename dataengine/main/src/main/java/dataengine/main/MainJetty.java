package dataengine.main;

import java.io.File;
import java.io.FileNotFoundException;
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
import dataengine.server.DeServerGuiceInjector;
import lombok.extern.slf4j.Slf4j;
import net.deelam.coordworkers.AmqServiceComp;

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
  private static final String BROKER_URL = DeServerGuiceInjector.brokerUrl4Java();

  public static void main(String[] args) throws Exception {
    String onlyRunServerProject = System.getProperty("ONLY_RUN_SERVER");
    boolean runInSingleJVM = (onlyRunServerProject == null) 
        ? true : Boolean.getBoolean(onlyRunServerProject);

    //System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "dataengine.api");
    if (runInSingleJVM) {
      log.info("======== Running all required DataEngine services in same JVM");
      try {
        startAllInSameJvm();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    MainJetty main = new MainJetty();
    
    // this contextPath mimics gretty's default behavior
    String contextPath="/main" // gretty uses the project name
        + "/DataEngine/0.0.3"; // matches the path in web.xml
        
    Server jettyServer = main.startServer(8080, 8083, contextPath,
        null, null, false); // TODO: 4: enable SSL

    try {
      jettyServer.start();
      jettyServer.join();
    } finally {
      jettyServer.destroy();
    }
  }

  public static void startAllInSameJvm() throws Exception {
    // TODO: replace with Zk-based component initialization
    AmqServiceComp amq=new AmqServiceComp();
    amq.start(DeServerGuiceInjector.properties());
    
    dataengine.sessions.SessionsMain.main(BROKER_URL);
    dataengine.tasker.TaskerMain.main(BROKER_URL);
    dataengine.jobmgr.JobManagerMain.main(BROKER_URL);
    dataengine.workers.WorkerMain.main(BROKER_URL);
  }

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
