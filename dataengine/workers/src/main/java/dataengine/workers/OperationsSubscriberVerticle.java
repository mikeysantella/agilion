package dataengine.workers;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;

import dataengine.apis.OperationsRegistry_I;
import net.deelam.vertx.HandlingSubscriberVerticle;

public class OperationsSubscriberVerticle extends HandlingSubscriberVerticle {

  public OperationsSubscriberVerticle(String serviceType, String serviceAddr, Worker_I worker) {
    super(serviceType, serviceAddr);
    this.worker=worker;
  }

  final Worker_I worker;
  
  @Override
  public void registerMsgHandlers() {
    registerMsgBeans(OperationsRegistry_I.msgBodyClasses);

    registerMsgHandler(QUERY_OPS.name(), (Object msgBody) -> worker.getOperations());
  }


}
