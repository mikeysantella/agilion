package dataengine.main;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.curator.framework.CuratorFramework;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
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
      System.setProperty(COMPONENT_IDS, args[0]);
    }
    try {
      startZkComponentStarter("startup.props");
    } catch (Exception e) {
      throw new IllegalStateException("While running myZkComponentStarterThread", e);
    }
    shutdown();
  }

  static CuratorFramework cf;
  static void shutdown() {
    if(cf!=null) 
      cf.close();
  }
  static List<String> startZkComponentStarter(String propFile) throws Exception {
    Properties properties=new Properties();
    PropertiesUtil.loadProperties(propFile, properties);

    //Configuration config = ConfigReader.parseFile(propFile);
    //log.info("{}\n------", ConfigReader.toStringConfig(config, config.getKeys()));

    String componentIds = System.getProperty(COMPONENT_IDS);
    if(componentIds==null || componentIds.length()==0)
      componentIds=properties.getProperty(COMPONENT_IDS, "");    

    List<String> compIdList =
        Arrays.stream(componentIds.split(",")).map(String::trim).collect(Collectors.toList());
    log.info("---------- componentIds to start: {}", compIdList);
    
    String zkConnectionString=properties.getProperty(ConstantsZk.ZOOKEEPER_CONNECT);
    String zkStartupPathHome=properties.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
    
    GModuleZkComponentStarter moduleZkComponentStarter =
        new GModuleZkComponentStarter(compIdList.size());
    Injector injector = Guice.createInjector( //
        new GModuleZooKeeper(zkConnectionString, zkStartupPathHome), //
        moduleZkComponentStarter);

    cf = injector.getInstance(CuratorFramework.class);
    String startupPath =
        injector.getInstance(Key.get(String.class, Names.named(ConstantsZk.ZOOKEEPER_STARTUPPATH)));

    // starts components given an compId and ComponentI subclass
    for (String compId : compIdList) {
      log.info("---------- Starting {}", compId);
      ZkComponentStarter.startComponent(injector, compId);
      log.info("Tree after starting {}: {}", compId, ZkConnector.treeToString(cf, startupPath));
    }

    long notStartedCount = moduleZkComponentStarter.getStartedLatch().getCount();
    do {
      log.info("---------- Waiting for components to start: {}", notStartedCount);
      moduleZkComponentStarter.getStartedLatch().await(1, TimeUnit.SECONDS);
      notStartedCount = moduleZkComponentStarter.getStartedLatch().getCount();
    } while (notStartedCount > 0);

    log.info("---------- All components started: {}", compIdList);
    log.info("Tree after all components started: {}", ZkConnector.treeToString(cf, startupPath));

    long compsStillRunning = moduleZkComponentStarter.getCompletedLatch().getCount();
    do {
      log.info("Waiting for components to end: {}", compsStillRunning);
      moduleZkComponentStarter.getCompletedLatch().await(1, TimeUnit.MINUTES);
      compsStillRunning = moduleZkComponentStarter.getCompletedLatch().getCount();
    } while (compsStillRunning > 0);

    log.info("---------- Tree after components stopped: {}", ZkConnector.treeToString(cf, startupPath));

    return compIdList;
  }
}