package dataengine.main;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import org.apache.curator.framework.CuratorFramework;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;
import net.deelam.zkbasedinit.ConstantsZk;
import net.deelam.zkbasedinit.GModuleZooKeeper;
import net.deelam.zkbasedinit.ZkConfigPopulator;
import net.deelam.zkbasedinit.ZkConnector;

@Slf4j
public class MainZkConfigPopulator {
  
  public static void main(String[] args) {
    String propsFile=(args.length>0)?args[0]:"dataengine.props";
    try {
      // must run in Thread to allow ZkComponentStarter to start required components before
      // ZkConfigs are fully populated
      boolean keepPrevConfig=Boolean.parseBoolean(System.getProperty("KEEP_PREV_ZKCONFIG"));
      startZookeeperConfigPopulator(propsFile, !keepPrevConfig);
    } catch (Exception e) {
      throw new IllegalStateException("While running MainZkConfigPopulator", e);
    }
    shutdown();
  }

  @Getter
  static CompletableFuture<String> componentIdsF = new CompletableFuture<>();

  static CuratorFramework cf;
  static void shutdown() {
    if(cf!=null)
      cf.close();
  }
  
  @Getter(lazy=true)
  private static final Properties properties = privateGetProperties();
  static String propFile;
  private static Properties privateGetProperties() {
    Properties properties = new Properties();
    try {
      PropertiesUtil.loadProperties(propFile, properties);
    } catch (IOException e) {
      log.warn("Couldn't load property file={}", propFile, e);
    }
    return properties;
  }
  
  static List<String> startZookeeperConfigPopulator(String propertyFile, boolean startFresh)
      throws Exception {
    propFile = propertyFile;

    String componentIds = System.getProperty(ZkConfigPopulator.COMPONENT_IDS);
    if(componentIds==null || componentIds.length()==0)
      componentIds=getProperties().getProperty(ZkConfigPopulator.COMPONENT_IDS, "");    
//    List<String> compIdList =
//        Arrays.stream(componentIds.split(",")).map(String::trim).collect(Collectors.toList());
//    log.info("---------- componentIds for configuration: {}", compIdList);

    Injector injector = Guice.createInjector(new GModuleZooKeeper(() -> getProperties()));
    cf = injector.getInstance(CuratorFramework.class);
    ZkConfigPopulator cp = injector.getInstance(ZkConfigPopulator.class);

    if (startFresh) {
      cp.cleanup();
      Thread.sleep(3000);
    }

    componentIdsF.complete(componentIds);
    
    List<String> compIdList=cp.populateConfigurations(propFile); //blocks until all required components started
    String zkStartupPathHome=System.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
    log.info("---------- Tree after config: {}",
        ZkConnector.treeToString(cf, zkStartupPathHome));

    return compIdList;
  }

}
