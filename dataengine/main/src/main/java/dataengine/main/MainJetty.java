package dataengine.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.jms.Connection;
import javax.jms.JMSException;
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
import net.deelam.activemq.ConstantsAmq;
import net.deelam.activemq.MQClient;
import net.deelam.activemq.rpc.AmqComponentRegistry;
import net.deelam.utils.ConsoleLogging;
import net.deelam.utils.ConsolePrompter;
import net.deelam.utils.PropertiesUtil;
import net.deelam.zkbasedinit.ConstantsZk;
import net.deelam.zkbasedinit.MainZkComponentStarter;
import net.deelam.zkbasedinit.MainZkComponentStopper;
import net.deelam.zkbasedinit.MainZkConfigPopulator;
import net.deelam.zkbasedinit.MainZookeeper;

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
  static final boolean DEBUG=false;
  static int SLEEPTIME=500;
  
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
  
  public static void main(String[] args) {
    prompter.setLog(ConsoleLogging.createSlf4jLogger("console.prompt"));

    boolean promptUser=Boolean.parseBoolean(System.getProperty("PROMPT"));
    if(!promptUser) {
      prompter.setSkipUserInput(true);
      prompter.setPrefix("--- Will ");
    }
    
    boolean runInSingleJVM = !Boolean.parseBoolean(System.getProperty("ONLY_RUN_SERVER"));

    int port = getSystemIntProperty("RESTPORT", 9090);
    int sslPort = getSystemIntProperty("RESTPORTSSL", 9093);
    if(isPortInUse(port)) {
      clog.error("Port {} is already in use!", port);
      System.exit(1);
    } else if(isPortInUse(sslPort)) {
      clog.error("Port {} is already in use!", port);
      System.exit(1);
    }
    
    MainJetty main=new MainJetty();
    //CompletableFuture<Integer> componentsF;
    //log.info("System.setProperty: {}={}", "org.apache.activemq.SERIALIZABLE_PACKAGES", "dataengine.api");
    //System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "dataengine.api");
    if (runInSingleJVM) {
      log.info("({}) ======== Running all required DataEngine services in same JVM", timer);
      try {
        main.startAllInSameJvm();
        String[] reqComponents = {"OperationsRegistry", "SessionsDB", "Tasker", "Dispatcher", "JobBoard"};
        List<String> compNames = main.compRegistry.get().waitForComponents(reqComponents);
        clog.info("Components: {}", compNames);
        main.startupExceptionF.complete(null);
        CompletableFuture<List<Map<String, Object>>> compAttribs = main.compRegistry.get().queryComponentAttributes();
        compAttribs.whenComplete((attribs,e)->{
          //if(DEBUG)
            log.info("Components attributes:\n\t{}", attribs.toString().replaceAll("\\}, \\{", "},\n\t{"));
        });
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        main.exitIfException();
      }
    }
    main.startJettyUntilEnds(port, sslPort);
  }

  public static int getSystemIntProperty(String key, int defVal) {
    int port=defVal;
    String propStr=System.getProperty(key);
    if(propStr!=null && propStr.length()>0)
      port=Integer.parseInt(propStr);
    return port;
  }

  Server jettyServer;
  private void startJettyUntilEnds(int port, int sslPort) {
    String contextPath=System.getProperty("RESTURLPATH");
    if(contextPath==null || contextPath.length()==0) {
      // this contextPath mimics gretty's default behavior
      contextPath="/main" // gretty uses the project name
          + "/DataEngine/0.0.3"; // matches the path in web.xml
    }
    prompter.getUserInput("start webserver on port="+port+" at RESTURLPATH="+contextPath, 2000);
    
    try {
      // TODO: 4: enable SSL
      jettyServer = startServer(port, sslPort, contextPath, null, null, false);
      try {
        try {
          jettyServer.start();
          clog.info("======== Data Engine REST server ready: startup time={}", timer);
          if (!hasExceptionSoFar())
            startPromptToShutdownThread(jettyServer);
          
          try {
            // wait for server to end
            jettyServer.join();
          } catch (InterruptedException e) {
            log.error("While waiting for REST server to send", e);
          }
        } catch (Exception e) {
          log.error("While starting up REST server", e);
          startupExceptionF.complete(e);
        }
      } finally {
        if (!jettyServer.isStopping()) {
          try {
            jettyServer.stop();
          } catch (Exception e) {
            log.error("While stopping REST server", e);
          }
          jettyServer.destroy();
        }
        log.info("Uptime={} ======== REST server is shutdown", timer);
      }
    } catch (FileNotFoundException e1) {
      log.error("While initializing REST server", e1);
      startupExceptionF.complete(e1);
    }
  }
  
  private boolean hasExceptionSoFar() {
    if (startupExceptionF.isDone()) {
      return (startupExceptionF.join() != null);
    } else
      return false;
  }
  
  private void exitIfException() {
    try {
      // once all components registered via AMQ, startupExceptionF.complete(null)
      Exception ex = startupExceptionF.get();
      if(ex!=null)
        throw ex;
    } catch (TimeoutException e) {
      log.info("No startup exceptions so far");
    } catch (Exception e) {
      clog.info("###################################################################");
      clog.info("############### Problem starting up Data Engine! ##################", e);
      try {
        // Allow other threads time to throw exception
        Thread.sleep(2000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      shutdownAll();
      System.exit(100);
    }
    clog.info("============= Data Engine components are ready ===================");
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
  
  CompletableFuture<Exception> startupExceptionF=new CompletableFuture<>(); 
  
  MainZookeeper zookeeper;
  final MainZkConfigPopulator configPopulator=new MainZkConfigPopulator(startupExceptionF::complete);
  final MainZkComponentStarter componentStarter = new MainZkComponentStarter(startupExceptionF::complete, startupExceptionF::complete);
  CompletableFuture<AmqComponentRegistry> compRegistry=new CompletableFuture<>();
  
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
      zookeeper.getZookeeperConnectF().get();
      // need to wait for Zookeeper to start
      clog.info("  (Waiting a few seconds for Zookeeper to start)");
      Thread.sleep(8*SLEEPTIME);
    }
    
//    String zkStartupPath = "/test/fromEclipse/startup";
//    log.info("System.setProperty: {}={}", ConstantsZk.ZOOKEEPER_STARTUPPATH, zkStartupPath);
//    System.setProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH, zkStartupPath);
    prompter.getUserInput("start MainZkConfigPopulator with ZOOKEEPER_STARTUPPATH", 3000);
    
    String configsPropsFilename=System.getProperty("PROPFILE");
    if(configsPropsFilename==null || configsPropsFilename.length()==0)
      configsPropsFilename=DATAENGINE_PROPS;
    final String configsPropsFile=configsPropsFilename;
    new Thread(() -> {
      configPopulator.SLEEPTIME=SLEEPTIME;
      configPopulator.DEBUG=true;
      configPopulator.startAndWaitUntilPopulated(configsPropsFile);
      log.info("({}) ======== Components configured", timer);
    }, "myZkConfigPopulator").start();

    Properties properties = PropertiesUtil.loadProperties(configsPropsFile);
    String componentIdsToStart = System.getProperty(MainZkComponentStarter.COMPONENT_IDS_TO_START);
    if(componentIdsToStart==null || componentIdsToStart.trim().length()==0) {
      clog.info("Populated configurations for components: {}", configPopulator.getComponentIdsF().get());
      if(true) {
        componentIdsToStart = properties.getProperty(MainZkComponentStarter.COMPONENT_IDS_TO_START);
      }else {
        // start all componentIds configured by MainZkConfigPopulator
        componentIdsToStart = configPopulator.getComponentIdsF().get();
      }
      log.info("System.setProperty: {}={}", MainZkComponentStarter.COMPONENT_IDS_TO_START, componentIdsToStart);
      System.setProperty(MainZkComponentStarter.COMPONENT_IDS_TO_START, componentIdsToStart);
    }
    prompter.getUserInput("start MainZkComponentStarter for: " + componentIdsToStart, 3000);
    new Thread(() -> {
      componentStarter.startAndWaitUntilStopped(STARTUP_PROPS);
      log.info("({}) ======== All started components have ended", timer);
    }, "myZkComponentStarter").start();

    prompter.getUserInput("start AmqComponentRegistry using " + configsPropsFile, 3000);
    startComponentRegistry(properties);
  }

  Connection connection;
  private void startComponentRegistry(Properties properties) throws FileNotFoundException, IOException, JMSException {
    String brokerUrl = ConstantsAmq.getTcpBrokerUrl(properties.getProperty("amq.brokerUrls"));
    
    Thread t = new Thread(() -> {
      try {
        // must create connection and AmqComponentRegistry in separate thread so that msgs can be
        // handled by a daemon subthreads that don't prevent shutdown 
        connection = MQClient.tryUntilConnect(brokerUrl);
        compRegistry.complete(new AmqComponentRegistry(connection));
      } catch (JMSException e) {
        log.warn("While running AmqComponentRegistry", e);
      }
    }, "myComponentRegistry");
    t.setDaemon(true);
    t.start();
  }

  private static final String TERMINATION_CHARS = "0LlQq";

  private void startPromptToShutdownThread(Server jettyServer) {
    Thread stopperThread = new Thread(() -> {
      prompter.setSkipUserInput(false);
      prompter.setPrefix(">>>>> ");
      String input = "";
      while (input == null || input.length() == 0 || !TERMINATION_CHARS.contains(input)) {
        input = prompter.getUserInput("Enter '0', 'Q', or 'L' to terminate application.", 1800_000);
      }
      shutdownAll();
    }, "myConsoleUiThread");
    stopperThread.setDaemon(true);
    stopperThread.start();
  }

  private void shutdownAll() {
    Stopwatch timer = Stopwatch.createStarted();
    clog.info("### Terminating: {} active threads", Thread.activeCount());
    prompter.shutdown();
    if (jettyServer != null)
      try {
        clog.info("(   0.0 s) ======== Stopping REST service");
        jettyServer.stop();
        DeServerGuiceInjector.shutdownSingleton();
      } catch (Exception e) {
        log.warn("While shutting down webserver", e);
      }

    try {
      if(connection!=null)
        connection.close();
    } catch (JMSException e) {
      log.warn("While shutting down AmqComponentRegistry", e);
    }
    clog.info("({}) ======== Shutting down components", timer);
    MainZkComponentStopper stopper = new MainZkComponentStopper();
    stopper.SLEEPTIME=SLEEPTIME;
    stopper.stopComponents(STARTUP_PROPS);
    configPopulator.shutdown();
    componentStarter.shutdown();

    if (zookeeper != null) {
      try {
        clog.info(
            "({})   (Waiting a few seconds to allow Zookeeper clients to shutdown before Zookeeper terminates)",
            timer);
        Thread.sleep(4 * SLEEPTIME); // Allowing components to shutdown before Zookeeper
      } catch (Exception e) {
      }
      clog.info("({}) ======== Shutting down Zookeeper (an InterruptedException is normal)", timer);
      zookeeper.shutdown();
    }

    if (DEBUG)
      checkRemainingThreads();
    clog.info("({}) ======== Done shutdown =========", timer);
  }

  static void checkRemainingThreads() {
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
