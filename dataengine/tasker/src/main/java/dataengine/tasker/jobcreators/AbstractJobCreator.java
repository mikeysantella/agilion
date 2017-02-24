package dataengine.tasker.jobcreators;

import java.util.HashMap;
import java.util.Map;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.tasker.JobsCreator;
import lombok.Getter;

public abstract class AbstractJobCreator implements JobsCreator {

  @Getter
  protected final Operation operation = new Operation();
  
  @Getter(lazy=true)
  private final Map<String, OperationParam> opParamsMap = initMap();

  protected Map<String, OperationParam> initMap(){
    Map<String, OperationParam> map = new HashMap<>();
    operation.getParams().forEach(p -> map.put(p.getKey(), p));
    return map;
  }
  
  @Override
  public OperationParam getOperationParam(String key) {
    return getOpParamsMap().get(key);
  }

}
