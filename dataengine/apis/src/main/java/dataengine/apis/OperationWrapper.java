package dataengine.apis;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import dataengine.api.Operation;
import dataengine.api.OperationMap;
import dataengine.api.OperationParam;
import dataengine.api.OperationSelection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;

@Slf4j
public final class OperationWrapper {

  @Getter
  protected Operation operation;

  //referenced operations keyed by operation.Id
  protected final Map<String, OperationWrapper> refOperations = new HashMap<>();

  public OperationWrapper(Operation operation, Operation... refOperations) {
    this(operation, Arrays.asList(refOperations));
  }

  public OperationWrapper(Operation operation, List<Operation> refOps) {
    this.operation = operation;

    OperationMap subOperationsMap = new OperationMap();
    operation.subOperations(subOperationsMap);
    for (Operation refOp : refOps) {
      subOperationsMap.put(refOp.getId(), refOp);
      refOperations.put(refOp.getId(), new OperationWrapper(refOp));
    }
    operationUpdated();
  }

  protected Map<String, OperationParam> opParamsMap;
  protected List<OperationParam> requiredParams;

  public void operationUpdated() {
    opParamsMap = initMap(operation);
    requiredParams = getRequiredParams(operation);
    refOperations.values().forEach(OperationWrapper::operationUpdated);

    //    List<String> missingSubOps = operation.getSubOperationIds().stream()
    //        .filter(subOpId -> !refOperations.containsKey(subOpId)).collect(toList());
    //    if (!missingSubOps.isEmpty())
    //      throw new IllegalArgumentException("Missing suboperations: " + missingSubOps);
  }

  public Set<String> getOperationParamKeys() {
    return opParamsMap.keySet();
  }

  public OperationParam getOperationParam(String key) {
    return opParamsMap.get(key);
  }

  public void checkForRequiredParams(OperationSelection operationSelection) {
    checkForRequiredParams(operationSelection.getId(), operationSelection.getParams());
    if (operationSelection.getSubOperationSelections() != null)
      operationSelection.getSubOperationSelections().values().forEach(opSel -> {
        OperationWrapper refOp = refOperations.get(opSel.getId());
        if(refOp!=null)
          refOp.checkForRequiredParams(opSel.getId(), opSel.getParams());
        else
          log.warn("Could not find '{}' in: {}", opSel.getId(), refOperations);
      });
  }

  public void checkForRequiredParams(String containerId, @SuppressWarnings("rawtypes") Map params) {
    log.debug("requiredParams={}", requiredParams);
    if (params == null)
      if (!requiredParams.isEmpty())
        throw new IllegalArgumentException(containerId + " required parameters missing: " +
            requiredParams.stream().map(OperationParam::getKey).collect(toList()));
      else
        return;

    List<OperationParam> missingParams =
        requiredParams.stream().filter((opParam) -> !params.containsKey(opParam.getKey())).collect(toList());
    if (!missingParams.isEmpty())
      throw new IllegalArgumentException(containerId + " missing required parameters: " +
          missingParams.stream().map(OperationParam::getKey).collect(toList()));
  }

  static List<OperationParam> getRequiredParams(Operation operation) {
    return operation.getParams().stream()
        .filter(OperationParam::getRequired).collect(toList());
  }

  static final Map<String, OperationParam> initMap(Operation operation) {
    Map<String, OperationParam> map = new HashMap<>();
    operation.getParams().forEach(p -> { 
      if(p.getValuetype()==null)
        throw new IllegalStateException("OperationParam needs valueType: "+p);
      map.put(p.getKey(), p);
    });
    return map;
  }

  @SuppressWarnings("unchecked")
  public void convertParamValues(OperationSelection opSel) {
    if (opSel.getParams() != null) {
      Map<String, Object> newMap = ((Map<String, Object>) opSel.getParams()).entrySet().stream()
          .collect(toMap(Map.Entry<String, Object>::getKey, e -> mapToValueClass(e)));
      opSel.setParams(newMap);
    }
    if (opSel.getSubOperationSelections() != null)
      opSel.getSubOperationSelections().values().stream()
          .forEach(subOpSel -> refOperations.get(subOpSel.getId()).convertParamValues(subOpSel));
  }

  Object mapToValueClass(Entry<String, Object> e) {
    OperationParam opParam = getOperationParam(e.getKey());
    if (opParam == null)
      throw new IllegalArgumentException(operation.getId()+": unknown parameter: " + e.getKey()+" params="+getOperationParamKeys());
    Object val = e.getValue();
    if (val == null) {
      if(opParam.getIsMultivalued())
        return Collections.emptyList();
      else
        return null;
    }
    if(opParam.getIsMultivalued()) {
      if(val instanceof String) {
        log.info("For param={}: converting String into List<{}>: {}", opParam.getKey(), opParam.getValuetype(), val);
        String listStr=(String) val;
        List<String> list=PropertiesUtil.splitList(",", listStr);
        val=list;
      }
      if(val instanceof List) {
        List<?> list=(List<?>) val;
        List<?> convertedList=list.stream().map(elem->convertToValueType(opParam, elem)).collect(Collectors.toList());
        return convertedList;
      } else {
        throw new UnsupportedOperationException("Cannot convert " + val.getClass() + " to " + opParam.getValuetype());
      }
    } else {
      return convertToValueType(opParam, val);
    }
  }

  public Object convertToValueType(OperationParam opParam, Object val) {
    if(opParam.getValuetype()==null)
      throw new IllegalStateException("OperationParam needs valueType: "+opParam);
    switch (opParam.getValuetype()) {
      case BOOLEAN:
        if (val instanceof Boolean)
          return val;
        if (val instanceof String)
          return Boolean.valueOf((String) val);
        break;
      case ENUM:
        if (val instanceof Enum)
          return val;
        if (val instanceof String)
          if (opParam.getPossibleValues().contains(val))
            return val;
          else
            throw new IllegalArgumentException(
                "Unknown enum='" + val + "'; possible values: " + opParam.getPossibleValues());
        break;
      case STRING:
        if (val instanceof String)
          return val;
        else
          return val.toString();
      case URI:
        if (val instanceof String)
          return URI.create((String) val);
      case INT:
        if (val instanceof Integer)
          return val;
        if (val instanceof Number)
          return ((Number) val).intValue();
        if (val instanceof String)
          return Integer.valueOf((String) val);
        break;
      case LONG:
        if (val instanceof Long)
          return val;
        if (val instanceof Number)
          return ((Number) val).longValue();
        if (val instanceof String)
          return Long.valueOf((String) val);
        break;
      case FLOAT:
        if (val instanceof Float)
          return val;
        if (val instanceof Number)
          return ((Number) val).floatValue();
        if (val instanceof String)
          return Float.valueOf((String) val);
        break;
      case DOUBLE:
        if (val instanceof Double)
          return val;
        if (val instanceof Number)
          return ((Number) val).doubleValue();
        if (val instanceof String)
          return Double.valueOf((String) val);
        break;
      case OPERATIONID:
        if (val instanceof String)
          return val;
        break;
      default:
        throw new UnsupportedOperationException("Cannot convert to " + opParam.getValuetype());
    }
    throw new UnsupportedOperationException("Cannot convert value=" + val + " to type=" + opParam.getValuetype());
  }

}
