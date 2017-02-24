package dataengine.tasker;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.Request;
import lombok.Data;
import lombok.experimental.Accessors;

public interface JobsCreator {

  Operation getOperation();

  OperationParam getOperationParam(String key);

  List<JobEntry> createFrom(Request addedReq);

  @Accessors(fluent = true)
  @Data
  static class JobEntry {
    Job job;
    String[] inputJobIds;
    
    public JobEntry(Job job, String... inputJobIds) {
      this.job = job;
      this.inputJobIds = inputJobIds;
    }
  }

  @SuppressWarnings("unchecked")
  default Map<String, Object> checkAndConvertParams(Request req) {
    checkForRequiredParams(req.getOperationParams());
    return convertParamValues(req.getOperationParams());
  }

  default void checkForRequiredParams(@SuppressWarnings("rawtypes") Map params) {
    List<OperationParam> missingParams = getOperation().getParams().stream().filter(OperationParam::getRequired)
        .filter((opParam) -> !params.containsKey(opParam.getKey())).collect(toList());
    if (missingParams.isEmpty())
      throw new IllegalArgumentException("Missing required parameters: " + missingParams);
  }

  default Map<String, Object> convertParamValues(Map<String, Object> inputParams) {
    Map<String, Object> newMap = inputParams.entrySet().stream()
        .collect(toMap(Map.Entry<String, Object>::getKey,
            e -> mapToValueClass(e)));
    return newMap;
  }

  default Object mapToValueClass(Entry<String, Object> e) {
    OperationParam opParam = getOperationParam(e.getKey());
    Object val=e.getValue();        
    switch(opParam.getValuetype()){
      case BOOLEAN:
        if(val instanceof Boolean)
          return val;
        if(val instanceof String)
          return Boolean.valueOf((String) val);
        break;
      case ENUM:
        if(val instanceof Enum)
          return val;
        if(val instanceof String)
          if(opParam.getPossibleValues().contains(val))
            return val;
          else
            throw new IllegalArgumentException("Unknown enum="+val+" possible values: "+opParam.getPossibleValues());
        break;
      case STRING:
        if(val instanceof String)
          return val;
        else
          return val.toString();
      case INT:
        if(val instanceof Integer)
          return val;
        if(val instanceof Number)
          return ((Number)val).intValue();
        if(val instanceof String)
          return Integer.valueOf((String) val);
        break;
      case LONG:
        if(val instanceof Long)
          return val;
        if(val instanceof Number)
          return ((Number)val).longValue();
        if(val instanceof String)
          return Long.valueOf((String) val);
        break;
      case FLOAT:
        if(val instanceof Float)
          return val;
        if(val instanceof Number)
          return ((Number)val).floatValue();
        if(val instanceof String)
          return Float.valueOf((String) val);
        break;
      case DOUBLE:
        if(val instanceof Double)
          return val;
        if(val instanceof Number)
          return ((Number)val).doubleValue();
        if(val instanceof String)
          return Double.valueOf((String) val);
        break;
      default:
        throw new UnsupportedOperationException("Cannot convert to "+opParam.getValuetype());
    }
    throw new UnsupportedOperationException("Cannot convert value="+val+" to type="+opParam.getValuetype());
  }

}
