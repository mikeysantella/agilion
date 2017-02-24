package dataengine.tasker.jobcreators;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.api.Request;
import jersey.repackaged.com.google.common.collect.Lists;

public class AddSourceDataset extends AbstractJobCreator {

  static final String ADD_SOURCE_DATASET = "addSourceDataset";

  private static final String INPUT_URI = "inputUri";
  private static final String DATA_FORMAT = "dataformat";

  public AddSourceDataset(Collection<Operation> ops) {
    operation.id(ADD_SOURCE_DATASET)
        .description("add source dataset");
    operation.addParamsItem(new OperationParam().key(INPUT_URI)
        .required(true)
        .description("location of source dataset")
        .valuetype(ValuetypeEnum.STRING).defaultValue(null).isMultivalued(false));
    
    //ops.stream().filter(op->op.getP);
    operation.addParamsItem(new OperationParam().key(DATA_FORMAT)
        .required(true)
        .description("type and format of data")
        .valuetype(ValuetypeEnum.ENUM).defaultValue(null).isMultivalued(false)
        // TODO: 1: retrieve possible ingest formats from workers
        .addPossibleValuesItem("TELEPHONE.CSV").addPossibleValuesItem("PEOPLE.CSV"));
  }

  @Override
  public List<JobEntry> createFrom(Request req) {
    checkArgument(operation.getId().equals(req.getOperationId()), "Operation.id does not match!");
    Map<String, Object> requestParams = checkAndConvertParams(req);
    
    Job job1 = new Job().id(req.getId() + ".job1")
        .requestId(req.getId())
        .label("Ingest " + requestParams.get(INPUT_URI))
        .params(requestParams);

    Job job2 = new Job().id(req.getId() + ".job2")
        .requestId(req.getId())
        .label("Ingest.PostProcess " + requestParams.get(INPUT_URI))
        .params(requestParams);

    return Lists.newArrayList(
        new JobEntry(job1),
        new JobEntry(job2, job1.getId()));
  }

}
