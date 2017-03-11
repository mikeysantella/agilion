package dataengine.apis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperationsMerger {
  
  private Map<String, Map<String, OperationParam>> operationParameters = new ConcurrentHashMap<>();

  public synchronized void clear() {
    operationParameters.clear();
  }

  @SuppressWarnings("unchecked")
  public synchronized Operation mergeOperation(Operation newOp, Operation op) {
    log.debug("mergeOperation: newOp={} op={}", newOp, op);
    if (op == null) {
      ConcurrentHashMap<String,OperationParam> paramsMapForOp = new ConcurrentHashMap<>();
      operationParameters.put(newOp.getId(), paramsMapForOp);
      newOp.getParams().forEach(p->{
        paramsMapForOp.put(p.getKey(), p);
      });
      return newOp;
    } else {
      op.description(mergeDescription(newOp.getDescription(), op.getDescription()));

      // TODO: 3: merge info maps
      if (newOp.getInfo() == null)
        op.info(newOp.getInfo());
      else
        op.getInfo().putAll(newOp.getInfo());

      if (newOp.getParams() != null)
        mergeParams(newOp.getParams(), operationParameters.get(op.getId()), op);

      return op;
    }
  }

  public static String mergeDescription(String newDescription, String description) {
    if (newDescription != null && !newDescription.equals(description))
      if (description == null)
        return newDescription;
      else
        return description + "; " + newDescription;
    return description;
  }

  public static void mergeParams(List<OperationParam> newParams, Map<String, OperationParam> paramsMap, Operation op) {
    newParams.forEach(newParam -> {
      OperationParam param = paramsMap.get(newParam.getKey());
      if (param == null) {
        param = newParam;
        op.addParamsItem(newParam);
        OperationParam oldParam = paramsMap.put(newParam.getKey(), newParam);
        if (oldParam != null)
          log.error("Not expecting existing oldParam={} newParam={}", oldParam, newParam);
      } else {        
        param.description(mergeDescription(newParam.getDescription(), param.getDescription()));

        if (newParam.getDefaultValue() != null &&
            !newParam.getDefaultValue().equals(param.getDefaultValue()))
          if (param.getDefaultValue() == null)
            param.defaultValue(newParam.getDefaultValue());
          else
            param.defaultValue(param.getDefaultValue() + "," + newParam.getDefaultValue());

        HashSet<Object> possValues = new HashSet<>(param.getPossibleValues());
        possValues.addAll(newParam.getPossibleValues());
        param.possibleValues(new ArrayList<>(possValues));

        param.required(pickOne(param.getKey(), "isRequired", newParam.getRequired(), param.getRequired(), Boolean.TRUE));

        param.isMultivalued(pickOne(param.getKey(), "isMultivalued", newParam.getIsMultivalued(), param.getIsMultivalued(), Boolean.TRUE));

        param.valuetype(pickOne(param.getKey(), "valueType", newParam.getValuetype(), param.getValuetype(), param.getValuetype()));
      }
    });
  }

  public static <T> T pickOne(String paramKey, String fieldName, T newObj, T obj, T defaultVal) {
    if (newObj == null)
      return obj;
    if (obj == null)
      return newObj;
    if (newObj.equals(obj))
      return obj;
    log.warn(paramKey+" parameter: conflicting '{}' field: {} != {}; choosing {}", 
        fieldName, newObj, obj, defaultVal);
    return defaultVal;
  }

}
