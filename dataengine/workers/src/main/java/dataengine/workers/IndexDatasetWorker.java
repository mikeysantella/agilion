package dataengine.workers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationConsts;
import lombok.Getter;

public class IndexDatasetWorker implements Worker_I {

  @Getter
  private String name = "IndexDatasetWorker-" + System.currentTimeMillis();

  @Override
  public Collection<Operation> getOperations() {
    ArrayList<Operation> list = new ArrayList<>();
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_POSTINGEST);
    list.add(new Operation()
        .id("INDEX_DATASET")
        .description("index source dataset")
        .info(info)
        .addParamsItem(new OperationParam()
            .key("inputUri").required(true)
            .description("location of input dataset")
            .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
            .defaultValue(null))
    //
    );
    return list;
  }

}
