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
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import jersey.repackaged.com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class ExportDataset extends AbstractJobCreator {

  //private final RpcClientProvider<SessionsDB_I> sessDb;
  
  @Override
  protected Operation initOperation() {
    Operation operation = new Operation().level(0).id(this.getClass().getSimpleName())
        .description("export dataset")
        .info(new HashMap<>());
    
    operation.addParamsItem(new OperationParam().key(OperationConsts.EXPORTER_WORKER)
        .required(true)
        .description("exporter worker to use")
        .valuetype(ValuetypeEnum.OPERATIONID));

    return operation;
  }

  public void updateOperationParams(Map<String, Operation> currOperations) {
    // copy exporter-type ops from workers
    List<Operation> exporterOps = copyOperationsOfType(currOperations, OperationConsts.TYPE_EXPORTER);
    opW = new OperationWrapper(initOperation(), exporterOps);

    opW.getOperationParam(OperationConsts.EXPORTER_WORKER).possibleValues(
        exporterOps.stream().map(Operation::getId).collect(toList()));

    opW.operationUpdated();
  }


  @SuppressWarnings("unchecked")
  @Override
  public List<JobEntry> createFrom(final Request req, List<String> priorJobIds) {
    OperationSelection selection = req.getOperation();
    checkArgument(opW.getOperation().getId().equals(selection.getId()), "Operation.id does not match!");
    opW.convertParamValues(selection);
    
    final String jobPrefix = genJobIdPrefix(req);
    Job job0 = new Job().id(jobPrefix + ".job0-preRequest")
        .type(OperationConsts.TYPE_PREREQUEST)
        .requestId(req.getId())
        .label("Pre-request job for: " + req.getLabel());
    Job job1 = new Job().id(jobPrefix + ".job1-export")
        .type(OperationConsts.TYPE_EXPORTER)
        .requestId(req.getId())
        .label("Export for: " + req.getLabel());
    Job job2 = new Job().id(jobPrefix + ".job2-postRequest")
        .type(OperationConsts.TYPE_POSTREQUEST)
        .requestId(req.getId())
        .label("Post-request job for: " + req.getLabel());

    String selectedOpId = (String) selection.getParams().get(OperationConsts.EXPORTER_WORKER);
    {
      Map<String, Object> job1Params = new HashMap<>(selection.getParams());
      if(selection.getSubOperationSelections()==null) {
        throw new IllegalArgumentException("No suboperation specified!");
      } else {
        OperationSelection exporterOpSelection = selection.getSubOperationSelections().get(selectedOpId);
        if(exporterOpSelection==null)
          throw new IllegalArgumentException("Not found '"+selectedOpId+"' in "+selection.getSubOperationSelections());
        job1Params.putAll(exporterOpSelection.getParams());
        job1Params.put(OperationConsts.WORKER_NAME, selectedOpId);
        job1.params(job1Params);
      }
    }
    {
      Map<String, Object> job3Params = new HashMap<>(selection.getParams());
      job3Params.put(OperationConsts.JOBID_OF_OUTPUT_DATASET, job1.getId());
      job2.params(job3Params);
    }

    return Lists.newArrayList(
        new JobEntry(job0, priorJobIds.toArray(new String[priorJobIds.size()])),
        new JobEntry(job1, job0.getId()),
        new JobEntry(job2, job1.getId()));
  }
}
