package dataengine.main;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.deelam.zkbasedinit.EmbeddedZookeeper;
import net.deelam.zkbasedinit.GModuleZooKeeper;

@Slf4j
public class MainZookeeper {
  public static void main(String[] args) {
    String configFile=(args.length>0)?args[0]:"zoo.cfg";
    try {
      zookeeper = new EmbeddedZookeeper(configFile);
      String connectionString = zookeeper.getConnectionString();
      System.setProperty(GModuleZooKeeper.ZOOKEEPER_CONNECT, connectionString);
      zookeeperConnectF.complete(connectionString);
      zookeeper.runServer(); // blocks until Zookeeper shuts down
      log.info("======== Zookeeper shut down.");
    } catch (Exception e) {
      throw new IllegalStateException("While running MainZookeeper", e);
    }
  }

  static EmbeddedZookeeper zookeeper;
  static void shutdown() {
    if(zookeeper!=null)
      zookeeper.stop();
  }
  
  static CompletableFuture<String> zookeeperConnectF = new CompletableFuture<>();

}
