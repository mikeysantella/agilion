package dataengine.workers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;

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
public class IngestTelephoneDummyWorker extends BaseWorker<Job> {

  @Inject
  public IngestTelephoneDummyWorker(RpcClientProvider<SessionsDB_I> sessDb) {
    super(OperationConsts.TYPE_INGESTER, sessDb);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    return new Operation().level(1).id(this.getClass().getSimpleName())
        .description("ingest source dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key(OperationConsts.INPUT_URI).required(true)
            .description("location of source dataset")
            .valuetype(ValuetypeEnum.URI).isMultivalued(false))
        .addParamsItem(new OperationParam()
            .key(OperationConsts.DATA_FORMAT).required(true)
            .description("type and format of data")
            .valuetype(ValuetypeEnum.ENUM).isMultivalued(false)
            .addPossibleValuesItem(MY_DATA_FORMAT))
        .addParamsItem(new OperationParam()
            .key(WORKTIME).required(true)
            .description("seconds the worker will take")
            .valuetype(ValuetypeEnum.INT).isMultivalued(false)
            .defaultValue(10));
  }

  private static final String WORKTIME = "workTime";
  private static final String MY_DATA_FORMAT = "TELEPHONE.CSV";

  @Override
  public boolean canDo(Job job) {
    return MY_DATA_FORMAT.equals(job.getParams().get(OperationConsts.DATA_FORMAT));
  }

  @Override
  protected boolean doWork(Job job) throws Exception {
    Dataset inDS = new Dataset()
        .uri((String) job.getParams().get(OperationConsts.INPUT_URI))
        .dataFormat((String) job.getParams().get(OperationConsts.DATA_FORMAT))
        .label("input for job " + job.getId());
    CompletableFuture<Dataset> addInputDsF = sessDb.rpc().addInputDataset(inDS, job.getId());

    String inputFilename = FilenameUtils.getBaseName(inDS.getUri());
    String outDsUri = "hdfs://cluster.domain.com:8020/tmp/" + job.getId() + "/" + inputFilename + "-ingested";
    Dataset outDS = new Dataset()
        .uri(outDsUri)
        .dataFormat(OperationConsts.DATA_FORMAT_PARQUET)
        .label("ingested dataset");
    CompletableFuture<Void> addOutputDsF =
        sessDb.rpc().addOutputDataset(outDS, job.getId()).thenAccept((addedOutDs) -> {
          state.getMetrics().put("ingested.dataset.id", addedOutDs.getId());
          state.getMetrics().put("ingested.dataset.uri", addedOutDs.getUri());
          sessDb.rpc().setJobParam(job.getId(), OperationConsts.OUTPUT_URI, addedOutDs.getId());
        });

    state.setPercent(2).setMessage("Determined input and output datasets");

    // do actual work
    AtomicInteger metricInt = new AtomicInteger();
    state.getMetrics().put("DUMMY_METRIC", metricInt);
    int numSeconds = (Integer) job.getParams().get(WORKTIME);
    for (int i = 0; i < numSeconds; ++i) {
      state.setPercent(i * (100 / (numSeconds + 1))).setMessage("DUMMY: status at percent=" + i * 18);
      metricInt.incrementAndGet();
      Thread.sleep(1000);
    }

    state.setPercent(99).setMessage("Done ingesting to " + outDsUri);

    // make sure these have finished before returning
    addInputDsF.get();
    addOutputDsF.get();
    return true;
  }
}
