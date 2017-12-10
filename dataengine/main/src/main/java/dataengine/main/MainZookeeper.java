package dataengine.main;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.deelam.zkbasedinit.ConstantsZk;
import net.deelam.zkbasedinit.EmbeddedZookeeper;

@Slf4j
public class MainZookeeper {
  public static void main(String[] args) {
    String configFile=(args.length>0)?args[0]:"zoo.cfg";
    try {
      log.info("ZK: Starting Zookeeper");
      zookeeper = new EmbeddedZookeeper(configFile);
      String connectionString = zookeeper.getConnectionString();
      log.info("System.setProperty: {}={}", ConstantsZk.ZOOKEEPER_CONNECT, connectionString);
      System.setProperty(ConstantsZk.ZOOKEEPER_CONNECT, connectionString);

      zookeeperConnectF.complete(connectionString);
      zookeeper.runServer(); // blocks until Zookeeper shuts down
      log.info("ZK: Stopped Zookeeper");
    } catch (Exception e) {
      throw new IllegalStateException("While running MainZookeeper", e);
    }
  }

  static EmbeddedZookeeper zookeeper;

  static boolean isRunning() {
    return zookeeper != null;
  }

  static void shutdown() {
    if (zookeeper != null) {
      log.info("ZK: Stopping Zookeeper");
      zookeeper.stop();
      zookeeper = null;
    }
  }
  
  static CompletableFuture<String> zookeeperConnectF = new CompletableFuture<>();

}
