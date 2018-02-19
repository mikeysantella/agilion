package dataengine.tasker.jobcreators;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.Request;
import dataengine.apis.OperationConsts;
import dataengine.apis.OperationUtils;
import dataengine.apis.OperationWrapper;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.tasker.JobsCreator_I;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.IdUtils;

@Slf4j
public abstract class AbstractJobCreator implements JobsCreator_I {

  protected OperationWrapper opW;

  public AbstractJobCreator() {
    opW=new OperationWrapper(initOperation());
  }
  
  public Operation getOperation(){
    return opW.getOperation();
  }
  
  protected Operation initOperation() {
    log.warn("TASKER: JobsCreator.operation not initialized: {}", this);
    // subclasses should populate operation
    return new Operation().level(0).id(this.getClass().getSimpleName());
  }

  @Override
  public void checkValidity(Request req) {
    opW.checkForRequiredParams(req.getOperation());
  }

  static String genJobIdPrefix(Request req) {
    return req.getId().substring(0,12) + "." + UUID.randomUUID();
  }

  public static List<Operation> copyOperationsOfType(Map<String, Operation> currOperations, String opType) {
    checkNotNull(currOperations);
    checkNotNull(opType);
    return currOperations.values().stream()
        .filter(op -> {
          if(op.getInfo()==null) {
            log.error("Operation should not have null info: {}", op);
            return false;
          }
          return opType.equals(op.getInfo().get(OperationConsts.OPERATION_TYPE));
        }).map(op -> OperationUtils.copy(op))
        .collect(toList());
  }

  public static void removeParamFromSubOperation(List<Operation> ops, String paramKey, Consumer<OperationParam> extract) {
    for (Operation op : ops) {
      for (Iterator<OperationParam> itr = op.getParams().iterator(); itr.hasNext();) {
        OperationParam opParam = itr.next();
        if (paramKey.equals(opParam.getKey())) {
          if(extract!=null) extract.accept(opParam);
          itr.remove();
        }
      }
    }
  }
}
