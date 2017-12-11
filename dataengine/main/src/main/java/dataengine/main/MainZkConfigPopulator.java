package dataengine.main;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.curator.framework.CuratorFramework;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;
import net.deelam.zkbasedinit.ConstantsZk;
import net.deelam.zkbasedinit.GModuleZooKeeper;
import net.deelam.zkbasedinit.ZkConfigPopulator;
import net.deelam.zkbasedinit.ZkConnector;

@RequiredArgsConstructor
@Slf4j
public class MainZkConfigPopulator {
  
  private final Consumer<Exception> exceptionHandler;
  
  public static void main(String[] args) {
    String propsFile=(args.length>0)?args[0]:"dataengine.props";
    new MainZkConfigPopulator((e) -> {
      e.printStackTrace();
      System.exit(1);
    }).startAndWaitUntilPopulated(propsFile);
  }
  
  public void startAndWaitUntilPopulated(String propsFile){
    try {
      // must run in Thread to allow ZkComponentStarter to start required components before
      // ZkConfigs are fully populated
      boolean keepPrevConfig=Boolean.parseBoolean(System.getProperty("KEEP_PREV_ZKCONFIG"));
      startZookeeperConfigPopulator(propsFile, !keepPrevConfig);
    } catch (Exception e) {
      if(exceptionHandler!=null)
        exceptionHandler.accept(e);
      throw new IllegalStateException("While running MainZkConfigPopulator", e);
    }
  }

  @Getter
  CompletableFuture<String> componentIdsF = new CompletableFuture<>();

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

  String propFile;
  private Properties privateGetProperties() {
    Properties properties = new Properties();
    try {
      PropertiesUtil.loadProperties(propFile, properties);
    } catch (IOException e) {
      log.warn("ZK: Couldn't load property file={}", propFile, e);
      if(exceptionHandler!=null)
        exceptionHandler.accept(e);
    }
    return properties;
  }
  
  void startZookeeperConfigPopulator(String propertyFile, boolean startFresh)
      throws Exception {
    propFile = propertyFile;

    String componentIds = System.getProperty(ZkConfigPopulator.COMPONENT_IDS);
    if(componentIds==null || componentIds.length()==0)
      componentIds=getProperties().getProperty(ZkConfigPopulator.COMPONENT_IDS, "");
    log.info("ZK: Components to set configuration: {}", componentIds);
    
    Injector injector = Guice.createInjector(new GModuleZooKeeper(() -> getProperties()));
    cf = injector.getInstance(CuratorFramework.class);
    ZkConfigPopulator cp = injector.getInstance(ZkConfigPopulator.class);

    String zkStartupPathHome=System.getProperty(ConstantsZk.ZOOKEEPER_STARTUPPATH);
    if (startFresh && ZkConnector.existsPath(cf, zkStartupPathHome)) {
      cp.cleanup();
      Thread.sleep(2*MainJetty.SLEEPTIME);
    }

    componentIdsF.complete(componentIds);
    
    List<String> compIdList=cp.populateConfigurations(propFile, exceptionHandler); //blocks until all required components started
    if(MainJetty.DEBUG) log.info("ZK: Tree after config: {}", ZkConnector.treeToString(cf, zkStartupPathHome));
    log.info("ZK: Done populating configuration for: {}", compIdList);

    shutdown();
    isDone.complete(true);
  }

}
