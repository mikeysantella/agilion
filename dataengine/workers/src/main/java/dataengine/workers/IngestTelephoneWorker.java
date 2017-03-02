package dataengine.workers;

import java.util.HashMap;
import java.util.Map;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationConsts;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class IngestTelephoneWorker extends BaseWorker<Object> {

  public IngestTelephoneWorker() {
    super(OperationConsts.TYPE_INGESTER);
  }

  {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    operations.add(new Operation()
        .id(jobType())
        .description("add source dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key("inputUri").required(true)
            .description("location of source dataset")
            .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
            .defaultValue(null))
        .addParamsItem(new OperationParam()
            .key(OperationConsts.INGEST_DATAFORMAT).required(true)
            .description("type and format of data")
            .valuetype(ValuetypeEnum.ENUM).isMultivalued(false)
            .defaultValue(null)
            .addPossibleValuesItem("TELEPHONE.CSV"))
    );
  }

  @Override
  protected boolean doWork(Object req) throws Exception {
    return super.doWork(req);
  }
}
