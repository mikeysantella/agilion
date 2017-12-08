package dataengine.main;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.commons.configuration2.Configuration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.zkbasedinit.ConfigReader;
import net.deelam.zkbasedinit.GModuleZooKeeper;
import net.deelam.zkbasedinit.ZkConfigPopulator;
import net.deelam.zkbasedinit.ZkConnector;

@Slf4j
public class MainZkConfigPopulator {
  private static final String COMPONENT_IDS = "componentIds";

  public static void main(String[] args) {
    String propsFile=(args.length>0)?args[0]:"dataengine.props";
    try {
      // must run in Thread to allow ZkComponentStarter to start required components before
      // ZkConfigs are fully populated
      boolean startFresh=System.getProperty("KEEP_PREV_ZKCONFIG")==null;
      startZookeeperConfigPopulator(propsFile, startFresh);
    } catch (Exception e) {
      throw new IllegalStateException("While running MainZkConfigPopulator", e);
    }
  }

  @Getter
  static CompletableFuture<String> componentIdsF = new CompletableFuture<>();

  static List<String> startZookeeperConfigPopulator(String propFile, boolean startFresh)
      throws Exception {
    Configuration config = ConfigReader.parseFile(propFile);
    //log.info("{}\n------", ConfigReader.toStringConfig(config, config.getKeys()));

    String componentIds = System.getProperty(COMPONENT_IDS, config.getString(COMPONENT_IDS, ""));
    List<String> compIdList =
        Arrays.stream(componentIds.split(",")).map(String::trim).collect(Collectors.toList());
    log.info("---------- componentIds for configuration: {}", compIdList);

    log.info("ZOOKEEPER_CONNECT=", System.getProperty(GModuleZooKeeper.ZOOKEEPER_CONNECT));
    Injector injector = Guice.createInjector(new GModuleZooKeeper(config));
    ZkConfigPopulator cp = injector.getInstance(ZkConfigPopulator.class);

    if (startFresh) {
      cleanup(cp);
      Thread.sleep(3000);
    }

    componentIdsF.complete(componentIds);
    
    cp.populateConfigurations(config, compIdList); //blocks until all required components started
    log.info("Tree after config: {}", ZkConnector.treeToString(cp.getClient(), cp.getAppPrefix()));

    return compIdList;
  }

  public static void cleanup(ZkConfigPopulator cp) throws Exception {
    log.info("cleanup: {}", cp.getAppPrefix());
    ZkConnector.deletePath(cp.getClient(), cp.getAppPrefix());
  }
}
