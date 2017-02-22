package dataengine.workers;

import java.util.ArrayList;
import java.util.List;
import static dataengine.apis.OperationsRegistry_I.OPERATIONS_REG_API.QUERY_OPS;


import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationsRegistry_I;
import net.deelam.vertx.HandlingSubscriberVerticle;

public class OperationsSubscriberVerticle extends HandlingSubscriberVerticle {

  public OperationsSubscriberVerticle(String serviceType, String serviceAddr) {
    super(serviceType, serviceAddr);
  }

  @Override
  public void registerMsgHandlers() {
    registerMsgBeans(OperationsRegistry_I.msgBodyClasses);
    
    registerMsgHandler(QUERY_OPS.name(), (Object msg)-> getOperations());
  }

  private String myId="-"+System.currentTimeMillis();
  private List<Operation> getOperations() {
    // TODO: 1: get operations from actual workers, if still alive
    ArrayList<Operation> list = new ArrayList<>();
    List<OperationParam> params=new ArrayList<>();
    list.add(new Operation().id("ADD_SOURCE_DATASET"+myId).description("add source dataset")
        .params(params));
    params.add(new OperationParam().key("inputUri").required(true).description("location of source dataset")
        .valuetype(ValuetypeEnum.STRING).defaultValue(null).isMultivalued(false));
    params.add(new OperationParam().key("dataformat").required(true).description("type and format of data")
        .valuetype(ValuetypeEnum.ENUM).defaultValue(null).isMultivalued(false)
        .addPossibleValuesItem("TELEPHONE.CSV").addPossibleValuesItem("PEOPLE.CSV")); 
    // TODO: 1: retrieve from Workers
    return list;
  }

}
