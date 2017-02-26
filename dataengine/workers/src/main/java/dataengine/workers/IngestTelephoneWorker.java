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

public class IngestTelephoneWorker implements Worker_I {

  @Getter
  private String name = "IngestTelephoneWorker-" + System.currentTimeMillis();

  @Override
  public Collection<Operation> getOperations() {
    ArrayList<Operation> list = new ArrayList<>();
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_INGESTER);
    list.add(new Operation()
        .id("INGEST_SOURCE_DATASET")
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
    //
    );
    return list;
  }

}
