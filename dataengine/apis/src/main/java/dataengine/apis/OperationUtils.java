package dataengine.apis;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OperationUtils {

  public static OperationParam removeOperationParam(Operation operation, String paramKey) {
    for(Iterator<OperationParam> it = operation.getParams().iterator(); it.hasNext();){
      OperationParam param = it.next();
      if(paramKey.equals(param.getKey())){
        it.remove();
        return param;
      }
    }
    return null;
  }

  public static void checkForRequiredParams(List<OperationParam> requiredParams, @SuppressWarnings("rawtypes") Map params) {
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

  public static List<OperationParam> getRequiredParams(Operation operation) {
    return operation.getParams().stream()
        .filter(OperationParam::getRequired).collect(toList());
  }

  public static final Map<String, OperationParam> initMap(Operation operation) {
    Map<String, OperationParam> map = new HashMap<>();
    operation.getParams().forEach(p -> map.put(p.getKey(), p));
    return map;
  }

}
