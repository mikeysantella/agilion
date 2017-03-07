package dataengine.workers;

import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;

import java.util.ArrayList;

import dataengine.api.Operation;
import dataengine.apis.OperationsRegistry_I;
import net.deelam.vertx.HandlingSubscriberVerticle;

public class OperationsSubscriberVerticle extends HandlingSubscriberVerticle {

  public OperationsSubscriberVerticle(String serviceType, String serviceAddr, Worker_I... workers) {
    super(serviceType, serviceAddr);
    this.workers = workers;
  }

  final Worker_I[] workers;

  @Override
  public void registerMsgHandlers() {
    registerMsgBeans(OperationsRegistry_I.msgBodyClasses);

    registerMsgHandler(QUERY_OPS.name(), (Object msgBody) -> {
      ArrayList<Operation> list = new ArrayList<>();
      for(Worker_I worker:workers)
      list.add(worker.operation());
      return list;
    });
  }


}
