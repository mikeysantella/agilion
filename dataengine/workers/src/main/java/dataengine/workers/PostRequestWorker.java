package dataengine.workers;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class PostRequestWorker extends BaseWorker<Job> {

  @Inject
  public PostRequestWorker(RpcClientProvider<SessionsDB_I> sessDb) {
    super(OperationConsts.TYPE_POSTREQUEST, sessDb);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_POSTINGEST);
    return new Operation()
        .id(jobType())
        .description("connect request to dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key(OperationConsts.PREV_JOBID).required(true)
            .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
            );
    
//    requiredParams = OperationUtils.getRequiredParams(getOperation());
  }

  @Override
  protected boolean doWork(Job job) throws Exception {
    getPrevJobDatasetId(job).thenAccept(datasetId->
      sessDb.rpc().connectAsOutputDatasetNode(job.getRequestId(), datasetId));
    return true;
  }

}
