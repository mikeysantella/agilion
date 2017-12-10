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

  static void stopZookeeperComponents(String propFile, boolean cleanUp)
      throws Exception {
    Properties properties = new Properties();
    PropertiesUtil.loadProperties(propFile, properties);

    String zkConnectionString = properties.getProperty(ConstantsZk.ZOOKEEPER_CONNECT);
    String zkStartupPathHome = properties.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);

    Injector injector =
        Guice.createInjector(new GModuleZooKeeper(zkConnectionString, zkStartupPathHome));

    ZkComponentStopper stopper = injector.getInstance(ZkComponentStopper.class);
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
