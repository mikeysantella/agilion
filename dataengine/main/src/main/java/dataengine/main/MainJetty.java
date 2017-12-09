package dataengine.main;

import java.io.File;
import java.io.FileNotFoundException;
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
import com.google.common.base.Stopwatch;
import dataengine.server.DeServerGuiceInjector;
import lombok.extern.slf4j.Slf4j;
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

  static ConsolePrompter prompter=new ConsolePrompter(">>>>>> ");
  static Stopwatch timer = Stopwatch.createStarted();
  public static void main(String[] args) throws Exception {
    boolean promptUser=Boolean.parseBoolean(System.getProperty("PROMPT"));
    prompter.setSkipPrompt(!promptUser);
    
    boolean runInSingleJVM = !Boolean.parseBoolean(System.getProperty("ONLY_RUN_SERVER"));

    //System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "dataengine.api");
    if (runInSingleJVM) {
      log.info("{} ======== Running all required DataEngine services in same JVM", timer);
      try {
        startAllInSameJvm();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    prompter.getUserInput("Press Enter to start webserver", 2000);
    MainJetty main = new MainJetty();
    String contextPath=System.getProperty("RESTURLPATH");
    if(contextPath==null || contextPath.length()==0) {
      // this contextPath mimics gretty's default behavior
      contextPath="/main" // gretty uses the project name
          + "/DataEngine/0.0.3"; // matches the path in web.xml
    }
    Server jettyServer = main.startServer(8080, 8083, contextPath,
        null, null, false); // TODO: 4: enable SSL
    
    try {
      jettyServer.start();
      log.info("======== Data Engine REST server ready: startup time={}", timer);
      startPromptToShutdownThread(jettyServer);
      jettyServer.join();
    } finally {
      if(!jettyServer.isStopping()) {
        jettyServer.stop();
        jettyServer.destroy();
      }
      log.info("Uptime={} ======== REST server is shutdown", timer);
    }
  }

  private static void startPromptToShutdownThread(Server jettyServer) {
    Thread stopperThread = new Thread(()->{
      prompter.setSkipPrompt(false);
      String input="";
      while(!"0".equals(input) && !"L".equals(input) && !"l".equals(input)) {
        input = prompter.getUserInput("Enter '0' or 'L' to terminate application.", 60000);
      }
      
      System.err.println("Terminating ...");
      log.info("Active threads: {}", Thread.activeCount());
      
      prompter.shutdown();
      try {
        jettyServer.stop();
        DeServerGuiceInjector.shutdownSingleton();
      } catch (Exception e) {
        log.warn("While shutting down webserver", e);
      }

      log.info("{} ======== Shutting down components", timer);
      MainZkComponentStopper.main(new String[0]);
      MainZkComponentStopper.shutdown();
      
      log.info("{} ======== Shutting down Zookeeper-related components", timer);
      MainZkConfigPopulator.shutdown();
      MainZkComponentStarter.shutdown();
      try {
        log.info("-- Sleeping to allow components to shutdown before Zookeeper");
        Thread.sleep(3000); // Allowing components to shutdown before Zookeeper
      } catch (Exception e) {
      }
      log.info("{} ======== Shutting down Zookeeper", timer);
      MainZookeeper.shutdown();
      
      //checkRemainingThreads();
      System.err.println("Done.");
    }, "myConsoleUiThread");
    stopperThread.setDaemon(true);
    stopperThread.start();
  }

  static void checkRemainingThreads() {
    int nonDaemonThreads;
    do{
      try {
        log.info("-- Sleeping to allowing threads to shutdown before checking for remaining threads");
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
  public static void startAllInSameJvm() throws Exception {
    File zkConfFile = new File("zoo.cfg");
    if (zkConfFile.exists()) {
      prompter.getUserInput("Press Enter to start MainZookeeper: " + zkConfFile, 3000);
      new Thread(() -> MainZookeeper.main(new String[] {zkConfFile.getAbsolutePath()}),
          "myEmbeddedZookeeperThread").start();
      log.info("Waiting for Zookeeper to start...");
      MainZookeeper.zookeeperConnectF.get();
      // need to wait for Zookeeper to start
      log.info("-- Sleeping to wait for Zookeeper to start");
      Thread.sleep(4000);
    }
    
    String zkStartupPath = "/test/fromEclipse/startup";
    System.setProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH, zkStartupPath);
    prompter.getUserInput("Press Enter to start MainZkConfigPopulator: ZOOKEEPER_STARTUPPATH=" + zkStartupPath, 3000);
    new Thread(() -> {
      String dataenginePropsFile=System.getProperty("PROPFILE");
      if(dataenginePropsFile==null || dataenginePropsFile.length()==0)
        dataenginePropsFile="dataengine.props";
      MainZkConfigPopulator.main(new String[] {dataenginePropsFile});
      log.info("{} ======== Components configured", timer);
    }, "myZkConfigPopulator").start();

    // need to wait for MainZkConfigPopulator to get further along
    log.info("-- Sleeping to wait for MainZkConfigPopulator to get further along");
    Thread.sleep(7000);

    // start all componentIds configured by MainZkConfigPopulator
    String componentIds = MainZkConfigPopulator.componentIdsF.get();
    prompter.getUserInput("Press Enter to start MainZkComponentStarter: " + componentIds, 3000);
    new Thread(() -> {
      MainZkComponentStarter.main(new String[] {componentIds});
      log.info("{} ======== All started components have ended", timer);
    }, "myZkComponentStarter").start();

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
