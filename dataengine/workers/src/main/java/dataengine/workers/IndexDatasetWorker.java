package dataengine.workers;

import java.util.HashMap;
import java.util.Map;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationConsts;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class IndexDatasetWorker extends BaseWorker<Object> {

  public IndexDatasetWorker(){
    super(OperationConsts.TYPE_POSTINGEST);
  }

  {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_POSTINGEST);
    operations.add(new Operation()
        .id(jobType())
        .description("index source dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key("inputUri").required(true)
            .description("location of input dataset")
            .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
            .defaultValue(null))
    );
  }

}
