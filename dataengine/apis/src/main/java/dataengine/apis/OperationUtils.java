package dataengine.apis;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.ObjectMapper;

import dataengine.api.Operation;
import dataengine.api.OperationParam;

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
  
  static ObjectMapper mapper = new ObjectMapper();
  public static Operation copy(Operation op){
    try {
      byte[] bytes = mapper.writeValueAsBytes(op);
      return mapper.readValue(bytes, Operation.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
//    return new Operation().id(op.getId())
//        .description(op.getDescription())
//        .info(new HashMap(op.getInfo()))
//        ;
  }

}
