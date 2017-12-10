package dataengine.main;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.curator.framework.CuratorFramework;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;
import net.deelam.zkbasedinit.ConstantsZk;
import net.deelam.zkbasedinit.GModuleZkComponentStarter;
import net.deelam.zkbasedinit.GModuleZooKeeper;
import net.deelam.zkbasedinit.ZkComponentStarter;
import net.deelam.zkbasedinit.ZkConnector;

@Slf4j
public class MainZkComponentStarter {
  public static final String COMPONENT_IDS = "componentIds";

  public static void main(String[] args) {
    if (args.length > 0) {
      log.info("System.setProperty: {}={}", COMPONENT_IDS, args[0]);
      System.setProperty(COMPONENT_IDS, args[0]);
    }
    
    new MainZkComponentStarter().startAndWaitUntilStopped("startup.props");
  }
  
  public void startAndWaitUntilStopped(String propertyFile){
    try {
      startZkComponentStarter(propertyFile);
    } catch (Exception e) {
      throw new IllegalStateException("While running myZkComponentStarterThread", e);
    }
  }

  private CuratorFramework cf;
  void shutdown() {
    if(cf!=null) {
      cf.close();
      cf=null;
    }
  }
  
  CompletableFuture<Boolean> isDone=new CompletableFuture<>(); 
  void blockUntilDone() {
    isDone.join();
  }
  
  @Getter(lazy=true)
  private final Properties properties = privateGetProperties();
  private String propFile;
  private Properties privateGetProperties() {
    Properties properties = new Properties();
    try {
      PropertiesUtil.loadProperties(propFile, properties);
    } catch (IOException e) {
      log.warn("ZK: Couldn't load property file={}", propFile, e);
    }
    return properties;
  }

  void startZkComponentStarter(String propertyFile) throws Exception {
    propFile = propertyFile;

    String componentIds = System.getProperty(COMPONENT_IDS);
    if(componentIds==null || componentIds.length()==0)
      componentIds=getProperties().getProperty(COMPONENT_IDS, "");    

    List<String> compIdList =
        Arrays.stream(componentIds.split(",")).map(String::trim).collect(Collectors.toList());
    log.info("ZK: ---------- componentIds to start: {}", compIdList);
    
    GModuleZkComponentStarter moduleZkComponentStarter =
        new GModuleZkComponentStarter(compIdList.size());
    Injector injector = Guice.createInjector( //
        new GModuleZooKeeper(() -> getProperties()), //
        moduleZkComponentStarter);

    cf = injector.getInstance(CuratorFramework.class);
    //String zkStartupPathHome=System.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
    String startupPath =
        injector.getInstance(Key.get(String.class, Names.named(ConstantsZk.ZOOKEEPER_STARTUPPATH)));

    // starts components given an compId and ComponentI subclass
    for (String compId : compIdList) {
      log.info("ZK: ---------- Starting {}", compId);
      ZkComponentStarter.startComponent(injector, compId);
      if(MainJetty.DEBUG) log.info("ZK: Tree after starting {}: {}", compId, ZkConnector.treeToString(cf, startupPath));
    }

    long notStartedCount = moduleZkComponentStarter.getStartedLatch().getCount();
    do {
      log.info("ZK: ---------- Waiting for components to start: {}", notStartedCount);
      moduleZkComponentStarter.getStartedLatch().await(1, TimeUnit.SECONDS);
      notStartedCount = moduleZkComponentStarter.getStartedLatch().getCount();
    } while (notStartedCount > 0);

    log.info("ZK: ---------- All components started: {}", compIdList);
    if(MainJetty.DEBUG) log.info("ZK: Tree after all components started: {}", ZkConnector.treeToString(cf, startupPath));

    long compsStillRunning = moduleZkComponentStarter.getCompletedLatch().getCount();
    do {
      log.info("ZK: Waiting for components to end: {}", compsStillRunning);
      moduleZkComponentStarter.getCompletedLatch().await(1, TimeUnit.MINUTES);
      compsStillRunning = moduleZkComponentStarter.getCompletedLatch().getCount();
    } while (compsStillRunning > 0);

    if(MainJetty.DEBUG) log.info("ZK: Tree after components stopped: {}", ZkConnector.treeToString(cf, startupPath));
    shutdown();
    log.info("ZK: ---------- Components ended: {}", compIdList);
    isDone.complete(true);
  }

}
