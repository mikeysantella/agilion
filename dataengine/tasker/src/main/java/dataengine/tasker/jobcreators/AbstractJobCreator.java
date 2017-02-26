package dataengine.tasker.jobcreators;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.Request;
import dataengine.tasker.JobsCreator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJobCreator implements JobsCreator {

  @Getter
  protected final Operation operation = new Operation();

  @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}) )
  private final Map<String, OperationParam> opParamsMap = _initMap();

  private final Map<String, OperationParam> _initMap() {
    Map<String, OperationParam> map = new HashMap<>();
    operation.getParams().forEach(p -> map.put(p.getKey(), p));
    return map;
  }

  @Override
  public OperationParam getOperationParam(String key) {
    return getOpParamsMap().get(key);
  }

  @Override
  public void checkValidity(Request req) {
    checkForRequiredParams(req.getOperationParams());
  }

  void checkForRequiredParams(@SuppressWarnings("rawtypes") Map params) {
    List<OperationParam> requiredParams =
        getOperation().getParams().stream().filter(OperationParam::getRequired).collect(toList());
    log.debug("requiredParams={}", requiredParams);
    if (params == null)
      if (!requiredParams.isEmpty())
        throw new IllegalArgumentException("Required parameters missing: " +
            requiredParams.stream().map(OperationParam::getKey).collect(toList()));
      else
        return;

    List<OperationParam> missingParams =
        requiredParams.stream().filter((opParam) -> !params.containsKey(opParam.getKey())).collect(toList());
    if (!missingParams.isEmpty())
      throw new IllegalArgumentException("Missing required parameters: " +
          missingParams.stream().map(OperationParam::getKey).collect(toList()));
  }

  String getJobIdPrefix(Request req) {
    return req.getId() + "-" + req.getLabel();
  }
}
