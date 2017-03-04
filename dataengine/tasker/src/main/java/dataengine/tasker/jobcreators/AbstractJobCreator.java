package dataengine.tasker.jobcreators;

import java.util.List;
import java.util.Map;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.Request;
import dataengine.apis.OperationUtils;
import dataengine.tasker.JobsCreator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJobCreator implements JobsCreator {

  @Getter
  protected final Operation operation;
  protected final Map<String, OperationParam> opParamsMap;
  protected final List<OperationParam> requiredParams;

  public AbstractJobCreator() {
    operation = initOperation();
    opParamsMap = OperationUtils.initMap(operation);
    requiredParams = OperationUtils.getRequiredParams(operation);
  }

  protected Operation initOperation() {
    log.warn("JobsCreator.operation not initialized: {}", this);
    // subclasses should populate operation
    return new Operation();
  }

  @Override
  public OperationParam getOperationParam(String key) {
    return opParamsMap.get(key);
  }

  @Override
  public void checkValidity(Request req) {
    OperationUtils.checkForRequiredParams(requiredParams, req.getOperationParams());
  }

  String getJobIdPrefix(Request req) {
    return req.getId() + "-" + req.getLabel();
  }
}
