package dataengine.tasker.jobcreators;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.apis.OperationConsts;
import dataengine.apis.OperationWrapper;
import jersey.repackaged.com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class AddSourceDataset extends AbstractJobCreator {

  @Override
  protected Operation initOperation() {
    Operation operation = new Operation().level(0).id(this.getClass().getSimpleName())
        .description("add source dataset");

    operation.addParamsItem(new OperationParam().key(OperationConsts.INPUT_URI)
        .required(true)
        .description("location of source dataset")
        .valuetype(ValuetypeEnum.URI));

    operation.addParamsItem(new OperationParam().key(OperationConsts.DATA_FORMAT)
        .required(true)
        .description("type and format of data")
        .valuetype(ValuetypeEnum.ENUM));
    
    operation.addParamsItem(new OperationParam().key(OperationConsts.INGESTER_WORKER)
        .required(true)
        .description("ingester worker to use")
        .valuetype(ValuetypeEnum.OPERATIONID));

    return operation;
  }

  public void updateOperationParams(Map<String, Operation> currOperations) {
    // copy ingester-type ops from workers
    List<Operation> ingesterOps = copyOperationsOfType(currOperations, OperationConsts.TYPE_INGESTER);

    // retrieve possible ingest formats and descriptions from ingester-type operations
    Set<Object> possibleDataFormats = new HashSet<>();
    Set<String> descriptions = new HashSet<>();
    removeParamFromSubOperation(ingesterOps, OperationConsts.DATA_FORMAT,(opParam)->{
      possibleDataFormats.addAll(opParam.getPossibleValues());
      descriptions.add(opParam.getDescription());
    });
    removeParamFromSubOperation(ingesterOps, OperationConsts.INPUT_URI,null);

    opW = new OperationWrapper(initOperation(), ingesterOps);
    OperationParam myOpDataFormatParam = opW.getOperationParam(OperationConsts.DATA_FORMAT);
    myOpDataFormatParam.possibleValues(new ArrayList<>(possibleDataFormats));
    myOpDataFormatParam.description(descriptions.toString());

    opW.getOperationParam(OperationConsts.INGESTER_WORKER).possibleValues(
        ingesterOps.stream().map(Operation::getId).collect(toList()));

    opW.operationUpdated();
  }


  @SuppressWarnings("unchecked")
  @Override
  public List<JobEntry> createFrom(final Request req, List<String> priorJobIds) {
    OperationSelection selection = req.getOperation();
    checkArgument(opW.getOperation().getId().equals(selection.getId()), "Operation.id does not match!");
    opW.convertParamValues(selection);
    
    final String jobPrefix = getJobIdPrefix(req); //req.getId() + "-" + req.getLabel();
    Job job0 = new Job().id(jobPrefix + ".job0-preRequest")
        .type(OperationConsts.TYPE_PREREQUEST)
        .requestId(req.getId())
        .label("Post-request " + req.getId() + ":" + req.getLabel());
    Job job1 = new Job().id(jobPrefix + ".job1-ingest")
        .type(OperationConsts.TYPE_INGESTER)
        .requestId(req.getId())
        .label("Ingest " + selection.getParams().get(OperationConsts.INPUT_URI));
    Job job2 = new Job().id(jobPrefix + ".job2-postIngest")
        .type(OperationConsts.TYPE_POSTINGEST)
        .requestId(req.getId())
        .label("Post-ingest " + selection.getParams().get(OperationConsts.INPUT_URI));
    Job job3 = new Job().id(jobPrefix + ".job3-postRequest")
        .type(OperationConsts.TYPE_POSTREQUEST)
        .requestId(req.getId())
        .label("Post-request " + req.getId() + ":" + req.getLabel());

    String selectedIngesterOpId = (String) selection.getParams().get(OperationConsts.INGESTER_WORKER);
    selection.getParams().remove(OperationConsts.INGESTER_WORKER); // don't need this after job1
    {
      Map<String, Object> job1Params = new HashMap<>(selection.getParams());
      OperationSelection ingesterOpSelection = selection.getSubOperationSelections().get(selectedIngesterOpId);
      if(ingesterOpSelection==null)
        throw new IllegalArgumentException("Not found '"+selectedIngesterOpId+"' in "+selection.getSubOperationSelections());
      job1Params.putAll(ingesterOpSelection.getParams());
      job1.params(job1Params);
    }
    selection.getParams().remove(OperationConsts.INPUT_URI); // don't need this after job1
    {
      Map<String, Object> job2Params = new HashMap<>(selection.getParams());
      job2Params.put(OperationConsts.PREV_JOBID, job1.getId());
      job2.params(job2Params);
    }
    selection.getParams().remove(OperationConsts.DATA_FORMAT); // don't need this any more
    {
      Map<String, Object> job3Params = new HashMap<>(selection.getParams());
      job3Params.put(OperationConsts.PREV_JOBID, job1.getId());
      job3.params(job3Params);
    }

    return Lists.newArrayList(
        new JobEntry(job0, priorJobIds.toArray(new String[priorJobIds.size()])),
        new JobEntry(job1, job0.getId()),
        new JobEntry(job2, job1.getId()),
        new JobEntry(job3, job2.getId()));
  }
}
