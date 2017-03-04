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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
public class IndexDatasetWorker extends BaseWorker<Job> {

  @Inject
  public IndexDatasetWorker(RpcClientProvider<SessionsDB_I> sessDb) {
    super(OperationConsts.TYPE_POSTINGEST, sessDb);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_POSTINGEST);
    return new Operation()
        .id(jobType())
        .description("index source dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key("inputUri").required(true)
            .description("location of input dataset")
            .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
            .defaultValue(null));
  }

  @Override
  protected boolean doWork(Job job) throws Exception {
    getPrevJobDatasetId(job).thenAccept(datasetId->{
      log.info("TODO: index {}", datasetId);
      try{
        Thread.sleep(1000);
      }catch(Exception e){
        e.printStackTrace();
      }
    });
    return true;
  }
  
}
