package dataengine.workers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.api.State;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
public class PostRequestWorker extends BaseWorker<Job> {

  @Inject
  public PostRequestWorker(RpcClientProvider<SessionsDB_I> sessDb) {
    super(OperationConsts.TYPE_POSTREQUEST, sessDb);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_POSTREQUEST);
    return new Operation().level(1).id(jobType())
        .description("connect request to dataset")
        .info(info)
//        .addParamsItem(new OperationParam()
//            .key(OperationConsts.PREV_JOBID).required(true)
//            .valuetype(ValuetypeEnum.STRING))
        .addParamsItem(new OperationParam()
            .key(OperationConsts.JOBID_OF_OUTPUT_DATASET).required(true)
            .valuetype(ValuetypeEnum.STRING));
  }

  @Override
  protected boolean doWork(Job job) throws Exception {
    String jobIdOfOutputDs = (String) job.getParams().get(OperationConsts.JOBID_OF_OUTPUT_DATASET);
    Collection<String> datasetIds=getJobOutputDatasetIds(jobIdOfOutputDs).get();
    log.info("datasetIds=", datasetIds);
    datasetIds.forEach(datasetId-> 
        sessDb.rpc().connectRequestToOutputDataset(job.getRequestId(), datasetId).join()
    );
    sessDb.rpc().updateRequestState(job.getRequestId(), State.COMPLETED);
    return true;
  }

}
