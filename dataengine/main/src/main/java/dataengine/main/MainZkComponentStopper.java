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
import net.deelam.zkbasedinit.ZkComponentStopper;
import net.deelam.zkbasedinit.ZkConnector;

@Slf4j
public class MainZkComponentStopper {

  public static void main(String[] args) {
    String propsFile = (args.length > 0) ? args[0] : "startup.props";
    try {
      boolean keepPrevConfig = Boolean.parseBoolean(System.getProperty("KEEP_PREV_ZKCONFIG"));
      stopZookeeperComponents(propsFile, !keepPrevConfig);
    } catch (Exception e) {
      throw new IllegalStateException("While running MainZkComponentStopper", e);
    }
    shutdown();
  }

  @Getter
  static CompletableFuture<String> componentIdsF = new CompletableFuture<>();

  static CuratorFramework cf;

  static void shutdown() {
    if (cf != null)
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
  
  static void stopZookeeperComponents(String propertyFile, boolean cleanUp)
      throws Exception {
    propFile = propertyFile;
    Injector injector = Guice.createInjector(new GModuleZooKeeper(() -> getProperties()));
    ZkComponentStopper stopper = injector.getInstance(ZkComponentStopper.class);
    
    String zkStartupPathHome=System.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
    log.info("---------- Tree before stopping: {}",
        ZkConnector.treeToString(cf, zkStartupPathHome));

    List<String> compIds = stopper.listRunningComponents();
    log.info("compIds to stop: {}", compIds);
    compIds.forEach(compId -> {
      try {
        stopper.stop(compId);
      } catch (Exception e) {
        log.error("When stopping compId=" + compId, e);
      }
    });

    try {
      Thread.sleep(2000); // allow time for modifications to take effect
      log.info("---------- Tree after stopping all components: {}",
          ZkConnector.treeToString(cf, zkStartupPathHome));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (cleanUp) {
      log.info("cleanup: {}", zkStartupPathHome);
      stopper.cleanup();
    }

    shutdown();
  }

}
