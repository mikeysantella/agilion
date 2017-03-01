package dataengine.apis;

import java.util.Iterator;

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

}
