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

public class IngestPeopleWorker implements Worker_I {

  @Getter
  private String name = "IngestPeopleWorker-" + System.currentTimeMillis();

  @Override
  public Collection<Operation> getOperations() {
    ArrayList<Operation> list = new ArrayList<>();
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    list.add(new Operation()
        .id("INGEST_SOURCE_DATASET")
        .description("add source dataset 2")
        .info(info)
        .addParamsItem(new OperationParam()
            .key("inputUri").required(true)
            .description("location of source dataset")
            .valuetype(ValuetypeEnum.STRING).isMultivalued(false)
            .defaultValue(null))
        .addParamsItem(new OperationParam()
            .key(OperationConsts.INGEST_DATAFORMAT).required(false)
            .description("type and format of data 2")
            .valuetype(ValuetypeEnum.ENUM).isMultivalued(true)
            .defaultValue(null)
            .addPossibleValuesItem("PEOPLE.CSV"))
    //
    );
    return list;
  }

}
