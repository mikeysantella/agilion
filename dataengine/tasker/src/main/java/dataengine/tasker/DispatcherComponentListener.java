package dataengine.tasker;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PathUtil;
import net.deelam.zkbasedinit.ZNodeListener;
import net.deelam.zkbasedinit.ZkComponentStarter;
import net.deelam.zkbasedinit.ZkConnector;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class DispatcherComponentListener {

  final CuratorFramework client;
  PathChildrenCache cache;
  
  public void shutdown() throws IOException {
    cache.close();
  }

  private final TaskerService taskerSvc;

  private void addDispatcher(String dispatcherRpcAddr) {
      taskerSvc.handleNewDepJobService(dispatcherRpcAddr);
  }

  private void removeDispatcher() {
    // TODO Auto-generated method stub
  }
  
  public void start(String path) {
    listenForNewDispatchers(path);
    handleCurrentDispatchers();
  }
  
  private void listenForNewDispatchers(String path) {
    cache = new PathChildrenCache(client, path, true);
    try {
      cache.start(StartMode.POST_INITIALIZED_EVENT);
      cache.getListenable().addListener((client2, event) -> {
        byte[] byteArr = (event.getData() == null) ? null : event.getData().getData();
        log.info("ZKCACHE EVENT: type={}, data={}", event.getType(),
            (byteArr == null) ? null : new String(byteArr));
        switch (event.getType()) {
          case CHILD_ADDED:
            getDispatcherRpcAddress(event.getData()).whenComplete((addr,e)->{
              if(addr!=null)
                addDispatcher(addr);
              else
                log.info("No dispatcher address yet: {}", e.getMessage());
            });
            break;
          case CHILD_UPDATED:
            handleNewDispatcher(event.getData());
            break;
          case CHILD_REMOVED:
            break;
          default:
            log.debug("ZKCACHE EVENT: {} initialData={}", event, event.getInitialData());
            break;
        }
      });
    } catch (Exception e) {
      throw new IllegalStateException("When setting up Zk-based cache for "+path, e);
    }
  }

  private void handleCurrentDispatchers() {
    List<ChildData> currChildren = cache.getCurrentData();
    log.info("CACHE: currData={}", currChildren);
    currChildren.forEach(cd->{
      handleNewDispatcher(cd);
    });
  }

  private void handleNewDispatcher(ChildData cd) {
    getDispatcherRpcAddress(cd).whenComplete((addr,e)->{
      if(addr!=null)
        addDispatcher(addr);
      else
        log.error("No dispatcher address", e);
    });
  }

  private CompletableFuture<String> getDispatcherRpcAddress(ChildData cd){
    final CompletableFuture<String> dispatcherRpcAddrF=new CompletableFuture<>();
    byte[] byteArr = cd.getData();
    if(byteArr==null) {
      dispatcherRpcAddrF.completeExceptionally(new IllegalStateException("Component path not yet set at: "+cd.getPath()));
    } else {
      String newPath = new String(byteArr);
      log.info("CACHE: Got newpath={} for {}", newPath, cd.getPath());
      String componentId = PathUtil.getFileName(newPath);
      String startedPath = newPath + ZkComponentStarter.STARTED_SUBPATH;
      
      try {
        boolean startedPathExists = client.checkExists().forPath(startedPath)!=null;
        if(startedPathExists) {
          getDispatcherAddrFromConfig(dispatcherRpcAddrF, newPath, componentId);
        } else {
          ZkConnector.watchForNodeChange(client, startedPath, new ZNodeListener() {
            @Override
            public void nodeCreated(String path) {
              getDispatcherAddrFromConfig(dispatcherRpcAddrF, newPath, componentId);
            }
          });
        }
      } catch (Exception e) {
        dispatcherRpcAddrF.completeExceptionally(e);
      }
    }
    return dispatcherRpcAddrF;
  }

  private void getDispatcherAddrFromConfig(CompletableFuture<String> dispatcherRpcAddrF,
      String compPath, String componentId) {
    try {
      Properties compConfig = ZkComponentStarter.getConfig(client, compPath, componentId);
      log.info("CACHE: compConfig={}", compConfig);
      String rpcAddr = compConfig.getProperty("msgQ.dispatcherRpcAddr");
      if(rpcAddr==null)
        dispatcherRpcAddrF.completeExceptionally(new IllegalArgumentException("Property 'msgQ.dispatcherRpcAddr' not found in "+compConfig));
      else
        dispatcherRpcAddrF.complete(rpcAddr);
    } catch (Exception e) {
      IllegalStateException e2 = new IllegalStateException("When reading component configuration in "+compPath, e);
      dispatcherRpcAddrF.completeExceptionally(e2);
      throw e2;
    }
  }

}
