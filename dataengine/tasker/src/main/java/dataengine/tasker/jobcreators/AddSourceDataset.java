package dataengine.tasker.jobcreators;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.api.Request;
import dataengine.apis.OperationConsts;
import jersey.repackaged.com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class AddSourceDataset extends AbstractJobCreator {

  static final String ADD_SOURCE_DATASET = "addSourceDataset";

  @Override
  protected Operation initOperation() {
    Operation operation=new Operation();
    operation.id(ADD_SOURCE_DATASET)
        .description("add source dataset");
    operation.addParamsItem(new OperationParam().key(OperationConsts.INPUT_URI)
        .required(true)
        .description("location of source dataset")
        .valuetype(ValuetypeEnum.STRING).defaultValue(null).isMultivalued(false));
    operation.addParamsItem(new OperationParam().key(OperationConsts.DATA_FORMAT)
        .required(true)
        .description("type and format of data")
        .valuetype(ValuetypeEnum.ENUM).defaultValue(null).isMultivalued(false));
    return operation;
  }
  
  public void updateOperationParams(Map<String, Operation> currOperations) {
    // retrieve possible ingest formats from workers
    Stream<Operation> ingesterOps = currOperations.values().stream()
        .filter(op -> OperationConsts.TYPE_INGESTER.equals(op.getInfo().get(OperationConsts.OPERATION_TYPE)));
    Stream<OperationParam> ingestDataFormatParams = ingesterOps
        .map(Operation::getParams)
        .flatMap(params -> params.stream()
            .filter(param -> OperationConsts.DATA_FORMAT.equals(param.getKey())));
    Set<Object> ingestDataFormats = ingestDataFormatParams
        .flatMap(param -> param.getPossibleValues().stream()).collect(toSet());
    OperationParam opParam = getOperationParam(OperationConsts.DATA_FORMAT);
    opParam.possibleValues(new ArrayList<>(ingestDataFormats));
  }
  
  @Override
  public List<JobEntry> createFrom(Request req) {
    checkArgument(operation.getId().equals(req.getOperationId()), "Operation.id does not match!");
    Map<String, Object> requestParams = convertParamValues(req.getOperationParams());

    Job job1 = new Job().id(getJobIdPrefix(req) + ".job1")
        .type(OperationConsts.TYPE_INGESTER)
        .requestId(req.getId())
        .label("Ingest " + requestParams.get(OperationConsts.INPUT_URI));
    Job job2 = new Job().id(req.getId() + "-" + req.getLabel() + ".job2")
        .type(OperationConsts.TYPE_POSTINGEST)
        .requestId(req.getId())
        .label("Post-ingest " + requestParams.get(OperationConsts.INPUT_URI));
    Job job3 = new Job().id(req.getId() + "-" + req.getLabel() + ".job3")
        .type(OperationConsts.TYPE_POSTREQUEST)
        .requestId(req.getId())
        .label("Post-request " + req.getId() + ":"+ req.getLabel());

    {
      Map<String, Object> job1Params = new HashMap<>(requestParams);
      job1.params(job1Params);
    }
    {
      Map<String, Object> job2Params = new HashMap<>(requestParams);
      job2Params.put(OperationConsts.PREV_JOBID, job1);
      job2.params(job2Params);
    }
    {
      Map<String, Object> job3Params = new HashMap<>(requestParams);
      job3Params.put(OperationConsts.PREV_JOBID, job2);
      job3.params(job3Params);
    }

    return Lists.newArrayList(
        new JobEntry(job1),
        new JobEntry(job2, job1.getId()),
        new JobEntry(job3, job2.getId())
        );
  }
}
