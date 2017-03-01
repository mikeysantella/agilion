package dataengine.tasker.jobcreators;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.api.Request;
import dataengine.apis.OperationConsts;
import jersey.repackaged.com.google.common.collect.Lists;

public class AddSourceDataset extends AbstractJobCreator {

  static final String ADD_SOURCE_DATASET = "addSourceDataset";

  private static final String INPUT_URI = "inputUri";
  private static final String DATA_FORMAT = "dataFormat";

  public AddSourceDataset(){
    operation.id(ADD_SOURCE_DATASET)
        .description("add source dataset");
    operation.addParamsItem(new OperationParam().key(INPUT_URI)
        .required(true)
        .description("location of source dataset")
        .valuetype(ValuetypeEnum.STRING).defaultValue(null).isMultivalued(false));
    operation.addParamsItem(new OperationParam().key(DATA_FORMAT)
        .required(true)
        .description("type and format of data")
        .valuetype(ValuetypeEnum.ENUM).defaultValue(null).isMultivalued(false));
  }
  
  public void update(Map<String, Operation> currOperations) {
    // retrieve possible ingest formats from workers
    Stream<Operation> ingesterOps = currOperations.values().stream()
        .filter(op -> OperationConsts.TYPE_INGESTER.equals(op.getInfo().get(OperationConsts.OPERATION_TYPE)));
    Stream<OperationParam> ingestDataFormatParams = ingesterOps
        .map(Operation::getParams)
        .flatMap(params -> params.stream()
            .filter(param -> OperationConsts.INGEST_DATAFORMAT.equals(param.getKey())));
    Set<Object> ingestDataFormats = ingestDataFormatParams
        .flatMap(param -> param.getPossibleValues().stream()).collect(toSet());
    OperationParam opParam = getOperationParam(DATA_FORMAT);
    opParam.possibleValues(new ArrayList<>(ingestDataFormats));
  }

  @Override
  public List<JobEntry> createFrom(Request req) {
    checkArgument(operation.getId().equals(req.getOperationId()), "Operation.id does not match!");
    Map<String, Object> requestParams = convertParamValues(req.getOperationParams());

    Job job1 = new Job().id(getJobIdPrefix(req) + ".job1")
        .type(OperationConsts.TYPE_INGESTER)
        .requestId(req.getId())
        .label("Ingest " + requestParams.get(INPUT_URI))
        .params(requestParams);

    Job job2 = new Job().id(req.getId() + "-" + req.getLabel() + ".job2")
        .type(OperationConsts.TYPE_POSTINGEST)
        .requestId(req.getId())
        .label("Post-ingest " + requestParams.get(INPUT_URI))
        .params(requestParams);

    return Lists.newArrayList(
        new JobEntry(job1),
        new JobEntry(job2, job1.getId()));
  }


}
