package net.deelam.vertx.rpc;

import com.xored.vertx.typed.rpc.EventBusServiceFactory;

import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.ClusteredVertxConfig;
import net.deelam.vertx.StartVertx;

@Slf4j
public class VertxRpcServer {
  
  public static void main(String[] args) {
    ClusteredVertxConfig config=new ClusteredVertxConfig();
    StartVertx.createClustered(config, vertx -> {
      log.info("Vert.x service registered");
      
      RemoteSvc remoteSvc = new RemoteSvc();
      //VertxRpcHelper.registerService(vertx, "Address", hdfsSvc);
      EventBusServiceFactory.registerServer(vertx.eventBus(), remoteSvc);
    });

  }

}
