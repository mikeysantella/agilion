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
import lombok.extern.slf4j.Slf4j;
import net.deelam.zkbasedinit.ZkComponentStopper;

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

  static ConsolePrompter prompter=new ConsolePrompter(">>>>>> ");
  public static void main(String[] args) throws Exception {
    boolean promptUser=true; //System.getProperty("PROMPT")!=null;
    prompter.setSkipPrompt(!promptUser);
    
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

    prompter.getUserInput("Press Enter to start webserver", 2000);
    MainJetty main = new MainJetty();
    // this contextPath mimics gretty's default behavior
    String contextPath="/main" // gretty uses the project name
        + "/DataEngine/0.0.3"; // matches the path in web.xml
        
    Server jettyServer = main.startServer(8080, 8083, contextPath,
        null, null, false); // TODO: 4: enable SSL

    promptToShutdown(jettyServer);
    
    try {
      jettyServer.start();
      jettyServer.join();
    } finally {
      if(!jettyServer.isStopped()) {
        jettyServer.stop();
        jettyServer.destroy();
      }
    }
  }

  private static void promptToShutdown(Server jettyServer) {
    new Thread(()->{
      prompter.setSkipPrompt(false);
      String input="";
      while(!"L".equals(input) && !"l".equals(input)) {
        input = prompter.getUserInput("Type 'L' then Enter to terminate application.", 60000);
      }
      System.err.println("Terminating ...");
      try {
        jettyServer.stop();
      } catch (Exception e) {
        log.warn("While shutting down webserver", e);
      }

      stopZkComponents();
      
      if(MainZookeeper.zookeeper!=null)
        MainZookeeper.zookeeper.stop();
      
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.err.println("Done.");
    }, "myConsoleUiThread").start();
  }

  private static void stopZkComponents() {
    try {
      ZkComponentStopper.main(new String[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Several services to start:
   * JVM1:
   * MainJetty = webserver = 'server' project
   * 
   * JVM2a:
   * Zookeeper (embedded or external)
   * 
   * JVM2b:
   * ZookeeperConfigPopulator
   * 
   * JVM3:
   * ZkComponentStarter: requires startup.props and componentIds to start
   *    starts AMQ 
   *    desired DataEngine components
   * 
   * JVM4:
   * ZkComponentStarter: requires startup.props and componentIds to start
   *    desired DataEngine components
   *  
   */
  public static void startAllInSameJvm() throws Exception {
    File zkConfFile = new File("zoo.cfg");
    if (zkConfFile.exists()) {
      prompter.getUserInput("Press Enter to start MainZookeeper: " + zkConfFile, 3000);
      new Thread(() -> MainZookeeper.main(new String[] {zkConfFile.getAbsolutePath()}),
          "myEmbeddedZookeeperThread").start();
      log.info("Waiting for Zookeeper to start...");
      MainZookeeper.zookeeperConnectF.get();
      // need to wait for Zookeeper to start
      Thread.sleep(2000);
    }
    
    String zkStartupPath = "/test/fromEclipse/startup";
    System.setProperty(net.deelam.zkbasedinit.Constants.ZOOKEEPER_STARTUPPATH, zkStartupPath);
    prompter.getUserInput("Press Enter to start MainZkConfigPopulator: " + zkStartupPath, 3000);
    new Thread(() -> MainZkConfigPopulator.main(new String[] {"dataengine.props"}),
        "myZkConfigPopulator").start();

    // need to wait for MainZkConfigPopulator to get further along
    Thread.sleep(3000);

    // start all componentIds configured by MainZkConfigPopulator
    String componentIds = MainZkConfigPopulator.componentIdsF.get();
    prompter.getUserInput("Press Enter to start MainZkComponentStarter: " + componentIds, 3000);
    new Thread(() -> MainZkComponentStarter.main(new String[] {componentIds}),
        "myZkConfigPopulator").start();

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
