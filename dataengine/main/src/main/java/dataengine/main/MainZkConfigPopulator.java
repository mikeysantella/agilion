package dataengine.main;

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
  
  static List<String> startZookeeperConfigPopulator(String propFile, boolean startFresh)
      throws Exception {
    Properties properties=new Properties();
    PropertiesUtil.loadProperties(propFile, properties);

    String componentIds = System.getProperty(ZkConfigPopulator.COMPONENT_IDS);
    if(componentIds==null || componentIds.length()==0)
      componentIds=properties.getProperty(ZkConfigPopulator.COMPONENT_IDS, "");    
//    List<String> compIdList =
//        Arrays.stream(componentIds.split(",")).map(String::trim).collect(Collectors.toList());
//    log.info("---------- componentIds for configuration: {}", compIdList);

    String zkConnectionString=properties.getProperty(ConstantsZk.ZOOKEEPER_CONNECT);
    String zkStartupPathHome=properties.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
    Injector injector = Guice.createInjector(new GModuleZooKeeper(zkConnectionString, zkStartupPathHome));
    cf = injector.getInstance(CuratorFramework.class);
    ZkConfigPopulator cp = injector.getInstance(ZkConfigPopulator.class);

    if (startFresh) {
      log.info("cleanup: {}", zkStartupPathHome);
      ZkConnector.deletePath(cf, zkStartupPathHome);
      Thread.sleep(3000);
    }

    componentIdsF.complete(componentIds);
    
    List<String> compIdList=cp.populateConfigurations(propFile); //blocks until all required components started
    log.info("---------- Tree after config: {}",
        ZkConnector.treeToString(cf, zkStartupPathHome));

    return compIdList;
  }

}
