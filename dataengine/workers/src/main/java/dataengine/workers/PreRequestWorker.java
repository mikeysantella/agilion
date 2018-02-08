package dataengine.workers;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.State;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class PreRequestWorker extends BaseWorker<Job> {

  @Inject
  public PreRequestWorker(RpcClientProvider<SessionsDB_I> sessDb) {
    super(OperationConsts.TYPE_PREREQUEST, sessDb);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_PREREQUEST);
    return new Operation().level(1).id(jobType())
        .description("initiate request waiting for prior requests to finish")
        .info(info);
  }

  @Override
  protected boolean doWork(Job job) throws Exception {
    sessDb.rpc().updateRequestState(job.getRequestId(), State.RUNNING);
    //connectF.get(); // call get so that exception can be thrown
    return true;
  }

}
