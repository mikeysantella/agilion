package dataengine.workers;

import java.util.HashMap;
import java.util.Map;

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
public class IndexDatasetDummyWorker extends BaseWorker<Job> {

  @Inject
  public IndexDatasetDummyWorker(RpcClientProvider<SessionsDB_I> sessDb) {
    super(OperationConsts.TYPE_POSTINGEST, sessDb);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_POSTINGEST);
    return new Operation().level(1).id(jobType())
        .description("index source dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key(OperationConsts.PREV_JOBID).required(true)
            .valuetype(ValuetypeEnum.STRING));
  }

  @Override
  protected boolean doWork(Job job) throws Exception {
    getPrevJobDatasetId(job).thenCompose(datasetId -> {
      log.info("WORKER: indexing {}", datasetId);
      return sessDb.rpc().getDataset(datasetId);
    }).thenCompose((inDS) -> {
      String inputFilename = FilenameUtils.getBaseName(inDS.getUri());
      String outDsUri = "hdfs://cluster.domain.com:8020/tmp/" + job.getId() + "/" + inputFilename + "-indexDB";
      Dataset outDS = new Dataset()
          .uri(outDsUri)
          .dataFormat(OperationConsts.DATA_FORMAT_LUCENE)
          .label("entity-index");
      return sessDb.rpc().addOutputDataset(outDS, job.getId());
    }).thenAccept(addedOutDs -> {
      sessDb.rpc().setJobParam(job.getId(), OperationConsts.OUTPUT_URI, addedOutDs.getId());
    }).get(); // call get so that exception can be thrown

    try {
      Thread.sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

}
