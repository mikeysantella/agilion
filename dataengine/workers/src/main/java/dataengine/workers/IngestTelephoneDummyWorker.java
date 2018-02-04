package dataengine.workers;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
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
        .description("ingest Telephone source dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key(OperationConsts.INPUT_NODELIST_URIS).required(false)
            .description("locations of nodelist files")
            .valuetype(ValuetypeEnum.URI).isMultivalued(true))
        .addParamsItem(new OperationParam()
            .key(OperationConsts.INPUT_EDGELIST_URIS).required(false)
            .description("locations of edgelist files")
            .valuetype(ValuetypeEnum.URI).isMultivalued(true))
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
    final HashMap<String, Object> statsMap = new HashMap<>();
    Dataset inDS = new Dataset()
        //.uri((String) job.getParams().get(OperationConsts.INPUT_URI))
        .dataFormat((String) job.getParams().get(OperationConsts.DATA_FORMAT))
        .label((String) job.getParams().get(OperationConsts.DATASET_LABEL)+"input for job " + job.getId())
        .stats(statsMap);
    
    log.info("WORKER: doWork on job={}", job);
    for(Object k:job.getParams().keySet()) {
      log.info(" param={}, value={} of class={}", k, job.getParams().get(k), job.getParams().get(k).getClass());
    }
    @SuppressWarnings("unchecked")
    final List<URI> nodelistUris = (List<URI>) job.getParams().get(OperationConsts.INPUT_NODELIST_URIS);
    for(int i=0; i<nodelistUris.size();++i) {
      statsMap.put(OperationConsts.INPUT_NODELIST_URIS+"."+i, nodelistUris.get(i));
    }
    @SuppressWarnings("unchecked")
    final List<URI> edgelistUris = (List<URI>) job.getParams().get(OperationConsts.INPUT_EDGELIST_URIS);
    for(int i=0; i<edgelistUris.size();++i) {
      statsMap.put(OperationConsts.INPUT_EDGELIST_URIS+"."+i, edgelistUris.get(i));
    }
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

    // do actual work on nodelistUris and edgelistUris
    AtomicInteger metricInt = new AtomicInteger();
    state.getMetrics().put("DUMMY_METRIC", metricInt);
    
    final int numFiles = nodelistUris.size() + edgelistUris.size()+1;
    int numSeconds = (Integer) job.getParams().get(WORKTIME);
    int filesIngested=0;
    for(int n=0; n<nodelistUris.size();++n) {
      ++filesIngested;
      state.setPercent(filesIngested*100/numFiles)
        .setMessage("DUMMY: ingesting "+nodelistUris.get(n));
      metricInt.incrementAndGet();
      Thread.sleep(numSeconds*1000);
    }
    for(int n=0; n<edgelistUris.size();++n) {
      ++filesIngested;
      state.setPercent(filesIngested*100/numFiles)
        .setMessage("DUMMY: ingesting "+edgelistUris.get(n));
      metricInt.incrementAndGet();
      Thread.sleep(numSeconds*1000);
    }

    state.setPercent(99).setMessage("Done ingesting to " + outDsUri);

    // make sure these have finished before returning
    addInputDsF.get();
    addOutputDsF.get();
    return true;
  }
}
