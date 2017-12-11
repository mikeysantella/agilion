package dataengine.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.slf4j.Logger;
import com.google.common.base.Stopwatch;
import dataengine.server.DeServerGuiceInjector;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.ConsoleLogging;
import net.deelam.zkbasedinit.ConstantsZk;

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
  static final boolean DEBUG=true;
  static int SLEEPTIME=0;
  
  static final Logger clog = ConsoleLogging.createSlf4jLogger(MainJetty.class);
  static ConsolePrompter prompter = new ConsolePrompter(">>>>> Press Enter to ");
  static Stopwatch timer = Stopwatch.createStarted();

  private static boolean isPortInUse(int port) {
    try {
      new ServerSocket(port).close();
      return false;
    } catch (IOException e) {
      return true;
    }
  }
  
  public static void main(String[] args) throws Exception {
    prompter.setLog(ConsoleLogging.createSlf4jLogger("console.prompt"));

    boolean promptUser=Boolean.parseBoolean(System.getProperty("PROMPT"));
    if(!promptUser) {
      prompter.setSkipUserInput(true);
      prompter.setPrefix("--- Will ");
    }
    
    boolean runInSingleJVM = !Boolean.parseBoolean(System.getProperty("ONLY_RUN_SERVER"));

    int port=9090;
    int sslPort = 9093;
    if(isPortInUse(port)) {
      clog.error("Port {} is already in use!", port);
      System.exit(1);
    } else if(isPortInUse(sslPort)) {
      clog.error("Port {} is already in use!", port);
      System.exit(1);
    }
    
    MainJetty main=new MainJetty();
    //log.info("System.setProperty: {}={}", "org.apache.activemq.SERIALIZABLE_PACKAGES", "dataengine.api");
    //System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "dataengine.api");
    if (runInSingleJVM) {
      log.info("({}) ======== Running all required DataEngine services in same JVM", timer);
      try {
        main.startAllInSameJvm();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    String contextPath=System.getProperty("RESTURLPATH");
    if(contextPath==null || contextPath.length()==0) {
      // this contextPath mimics gretty's default behavior
      contextPath="/main" // gretty uses the project name
          + "/DataEngine/0.0.3"; // matches the path in web.xml
    }
    prompter.getUserInput("start webserver on port="+port+" at RESTURLPATH="+contextPath, 2000);
    
    Server jettyServer = main.startServer(port, sslPort, contextPath,
        null, null, false); // TODO: 4: enable SSL
    
    try {
      jettyServer.start();
      log.info("======== Data Engine REST server ready: startup time={}", timer);
      
      // TODO: Create AMQ-based listener/querier to check state of and list components 
      clog.info("======== Ready!  Startup time="+timer);
      main.startPromptToShutdownThread(jettyServer);
      jettyServer.join();
    } finally {
      if(!jettyServer.isStopping()) {
        jettyServer.stop();
        jettyServer.destroy();
      }
      log.info("Uptime={} ======== REST server is shutdown", timer);
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
   * @param dataenginePropsFile 
   *  
   */
  static MainZookeeper zookeeper;
  static final MainZkConfigPopulator configPopulator=new MainZkConfigPopulator();
  static final MainZkComponentStarter componentStarter = new MainZkComponentStarter();

  private static final String DATAENGINE_PROPS = "dataengine.props";
  private static final String STARTUP_PROPS = "startup.props";
  
  public void startAllInSameJvm() throws Exception {
    File zkConfFile = new File("zoo.cfg");
    if (zkConfFile.exists()) {
      SLEEPTIME=1000;
      prompter.getUserInput("start MainZookeeper using " + zkConfFile, 3000);
      zookeeper = new MainZookeeper();
      new Thread(() -> zookeeper.startAndWaitUntilStopped(zkConfFile.getAbsolutePath()),
          "myEmbeddedZookeeperThread").start();
      log.info("Waiting for Zookeeper to start...");
      zookeeper.zookeeperConnectF.get();
      // need to wait for Zookeeper to start
      clog.info("  (Waiting a few seconds for Zookeeper to start)");
      Thread.sleep(8*SLEEPTIME);
    }
    
    String zkStartupPath = "/test/fromEclipse/startup";
    log.info("System.setProperty: {}={}", ConstantsZk.ZOOKEEPER_STARTUPPATH, zkStartupPath);
    System.setProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH, zkStartupPath);
    prompter.getUserInput("start MainZkConfigPopulator with ZOOKEEPER_STARTUPPATH=" + zkStartupPath, 3000);
    new Thread(() -> {
      String configsPropsFile=System.getProperty("PROPFILE");
      if(configsPropsFile==null || configsPropsFile.length()==0)
        configsPropsFile=DATAENGINE_PROPS;
      configPopulator.startAndWaitUntilPopulated(configsPropsFile);
      log.info("({}) ======== Components configured", timer);
    }, "myZkConfigPopulator").start();

    String componentIds = System.getProperty(MainZkComponentStarter.COMPONENT_IDS);
    if(componentIds==null || componentIds.length()==0) {
      // start all componentIds configured by MainZkConfigPopulator
      componentIds = configPopulator.getComponentIdsF().get();
      log.info("System.setProperty: {}={}", MainZkComponentStarter.COMPONENT_IDS, componentIds);
      System.setProperty(MainZkComponentStarter.COMPONENT_IDS, componentIds);
    }
    prompter.getUserInput("start MainZkComponentStarter for: " + componentIds, 3000);
    new Thread(() -> {
      componentStarter.startAndWaitUntilStopped(STARTUP_PROPS);
      log.info("({}) ======== All started components have ended", timer);
    }, "myZkComponentStarter").start();

  }

  private static final String TERMINATION_CHARS="0LlQq"; 
  private void startPromptToShutdownThread(Server jettyServer) {
    Thread stopperThread = new Thread(()->{
      prompter.setSkipUserInput(false);
      prompter.setPrefix(">>>>> ");
      String input="";
      while(input==null || input.length()==0 || !TERMINATION_CHARS.contains(input)) {
        input = prompter.getUserInput("Enter '0', 'Q', or 'L' to terminate application.", 1800_000);
      }
      
      timer.reset();
      timer.start();
      clog.info("### Terminating: {} active threads", Thread.activeCount());
      prompter.shutdown();
      try {
        clog.info("(  0.0 s) ======== Stopping REST service");
        jettyServer.stop();
        DeServerGuiceInjector.shutdownSingleton();
      } catch (Exception e) {
        log.warn("While shutting down webserver", e);
      }

      clog.info("({}) ======== Triggering shutdown", timer);
      new MainZkComponentStopper().stopComponents(STARTUP_PROPS);
      
      clog.info("({}) ======== Shutting down components", timer);
      configPopulator.shutdown();
      componentStarter.shutdown();
      
      if(zookeeper!=null){
        try {
          clog.info("({})   (Waiting a few seconds to allow Zookeeper clients to shutdown before Zookeeper terminates)", timer);
          Thread.sleep(5*SLEEPTIME); // Allowing components to shutdown before Zookeeper
        } catch (Exception e) {
        }
        clog.info("({}) ======== Shutting down Zookeeper (an InterruptedException is normal)", timer);
        zookeeper.shutdown();
      }
      
      if(DEBUG) checkRemainingThreads();
      clog.info("({}) ======== Done.", timer);
    }, "myConsoleUiThread");
    stopperThread.setDaemon(true);
    stopperThread.start();
  }

  void checkRemainingThreads() {
    int nonDaemonThreads;
    do{
      try {
        log.info("Sleeping to allowing threads to shutdown before checking for remaining threads");
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      Set<Thread> ndThreads = Thread.getAllStackTraces().keySet().stream().filter(th->!th.isDaemon()).collect(Collectors.toSet());
      nonDaemonThreads=ndThreads.size();
      for(Thread th:ndThreads){
        synchronized(th) {
          th.notify();
        }
        th.interrupt();
        log.warn("{} {}: {}", th, th.getState(), Arrays.toString(th.getStackTrace()).replaceAll(",", "\n\t"));
      };
      log.info("{}/{} remaining non-daemon threads: {}", nonDaemonThreads, Thread.activeCount(), ndThreads);
    }while(nonDaemonThreads>1);
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
