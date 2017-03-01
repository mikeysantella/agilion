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
import lombok.experimental.Accessors;
import net.deelam.vertx.jobboard.ProgressState;

@Accessors(fluent = true)
public class IndexDatasetWorker implements Worker_I {

  @Getter
  private String name = "IndexDatasetWorker-" + System.currentTimeMillis();

  @Getter
  public String jobType = "INGEST_SOURCE_DATASET";

  @Getter
  public ProgressState state = new ProgressState();

  @Getter
  public Collection<Operation> operations = new ArrayList<>();

  {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_POSTINGEST);
    operations.add(new Operation()
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
  }

}
