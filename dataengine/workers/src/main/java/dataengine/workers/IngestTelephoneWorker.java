package dataengine.workers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dataengine.api.Dataset;
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
public class IngestTelephoneWorker extends BaseWorker<Job> {

  @Inject
  public IngestTelephoneWorker(RpcClientProvider<SessionsDB_I> sessDb) {
    super(OperationConsts.TYPE_INGESTER, sessDb);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    return new Operation()
        .id(jobType())
        .description("add source dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key("inputUri").required(true)
            .description("location of source dataset")
            .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
            .defaultValue(null))
        .addParamsItem(new OperationParam()
            .key(OperationConsts.DATA_FORMAT).required(true)
            .description("type and format of data")
            .valuetype(ValuetypeEnum.ENUM).isMultivalued(false)
            .defaultValue(null)
            .addPossibleValuesItem("TELEPHONE.CSV"));
  }

  @Override
  protected boolean doWork(Job job) throws Exception {
    log.info("TODO: implement doWork(): {}", job);
    Dataset inDS=new Dataset()
        .uri((String) job.getParams().get(OperationConsts.INPUT_URI))
        .dataFormat((String) job.getParams().get(OperationConsts.DATA_FORMAT))
        .label("input for job="+job.getId());
    sessDb.rpc().addInputDataset(inDS, job.getId());
    
    AtomicInteger metricInt = new AtomicInteger();
    state.getMetrics().put("DUMMY_METRIC", metricInt);
    int numSeconds = 30;
    for (int i = 0; i < numSeconds; ++i) {
      state.setPercent(i * (100 / (numSeconds + 1))).setMessage("DUMMY: status at percent=" + i * 18);
      metricInt.incrementAndGet();
      Thread.sleep(1000);
    }
    
    String outDsUri="hdfs://cluster.domain.com:8020/tmp/"+job.getId()+"/output.parquet";
    Dataset outDS=new Dataset()
        .uri(outDsUri)
        .dataFormat(OperationConsts.DATA_FORMAT_PARQUET)
        .label("ingested dataset");
    sessDb.rpc().addOutputDataset(outDS, job.getId());
    
    sessDb.rpc().setJobParam(job.getId(), OperationConsts.OUTPUT_URI, outDS.getId());
    return true;
  }
}
