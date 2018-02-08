package dataengine.tasker;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationConsts;
import dataengine.apis.OperationsMerger;

public class OperationsMergerTest {

  @Before
  public void setUp() throws Exception {}

  public List<Operation> getPerOperations() {
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
            .key(OperationConsts.DATA_FORMAT).required(false)
            .description("type and format of data 2")
            .valuetype(ValuetypeEnum.ENUM).isMultivalued(true)
            .defaultValue(null)
            .addPossibleValuesItem("PEOPLE.CSV"))
    //
    );
    return list;
  }
  public List<Operation> getTeleOperations() {
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
            .key(OperationConsts.DATA_FORMAT).required(true)
            .description("type and format of data")
            .valuetype(ValuetypeEnum.ENUM).isMultivalued(false)
            .defaultValue(null)
            .addPossibleValuesItem("TELEPHONE.CSV"))
    //
    );
    return list;
  }
  @Test
  public void test() {
    Operation opTel = getTeleOperations().get(0);
    Operation opPer = getPerOperations().get(0);
    OperationsMerger merger = new OperationsMerger();
    merger.mergeOperation(opTel, null);
    merger.mergeOperation(opPer, opTel);
    System.out.println(opTel);
    
    Operation opTel2 = getTeleOperations().get(0);
    Operation opPer2 = getPerOperations().get(0);
    OperationsMerger merger1 = new OperationsMerger();
    merger1.mergeOperation(opPer2, null);
    merger1.mergeOperation(opTel2, opPer2);
    System.out.println(opPer2);

    // description is order dependent
    opTel.setDescription(opPer2.getDescription());
    opTel.getParams().get(1).setDescription(
        opPer2.getParams().get(1).getDescription());
    
    assertEquals(opTel, opPer2);
  }

}
