package dataengine.workers;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
public class IngestPeopleDummyWorker extends BaseWorker<Job> {

  @Inject
  public IngestPeopleDummyWorker(RpcClientProvider<SessionsDB_I> sessDb) {
    super(OperationConsts.TYPE_INGESTER, sessDb);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    return new Operation().level(1).id(jobType())
        .info(info)
        .addParamsItem(new OperationParam()
            .key(OperationConsts.INPUT_URI).required(true))
        .addParamsItem(new OperationParam()
            .key(OperationConsts.DATA_FORMAT)
            .description("choosing '"+MY_DATA_FORMAT+"' will always fail")
            .isMultivalued(true)
            .addPossibleValuesItem(MY_DATA_FORMAT));
  }

  private static final String MY_DATA_FORMAT = "PEOPLE.CSV";

  @Override
  public boolean canDo(Job job) {
    return MY_DATA_FORMAT.equals(job.getParams().get(OperationConsts.DATA_FORMAT));
  }
  
  @Override
  protected boolean doWork(Job job) throws Exception {
    log.info("WORKER: doWork() but will always fail: {} {}", this, job);
    return false;
  }
}
