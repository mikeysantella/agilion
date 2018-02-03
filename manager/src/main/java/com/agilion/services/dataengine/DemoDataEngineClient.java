package com.agilion.services.dataengine;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataengine.api.Operation;
import jersey.repackaged.com.google.common.collect.Lists;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex_Lappy_486 on 2/1/18.
 */
public class DemoDataEngineClient implements DataEngineClient
{
    private static final String listOperationJson = "[{\"id\":\"AddSourceDataset\",\"description\":\"add source dataset\",\"level\":0,\"params\":[{\"key\":\"inputUri\",\"description\":\"location of source dataset\",\"valuetype\":\"URI\",\"required\":true,\"isMultivalued\":false,\"possibleValues\":[]},{\"key\":\"dataFormat\",\"description\":\"[choosing \\u0027PEOPLE.CSV\\u0027 will always fail, type and format of data]\",\"valuetype\":\"ENUM\",\"required\":true,\"isMultivalued\":false,\"possibleValues\":[\"PEOPLE.CSV\",\"TELEPHONE.CSV\"]},{\"key\":\"ingesterWorker\",\"description\":\"ingester worker to use\",\"valuetype\":\"OPERATIONID\",\"required\":true,\"isMultivalued\":false,\"possibleValues\":[\"IngestTelephoneDummyWorker\",\"IngestPeopleDummyWorker\"]}],\"subOperations\":{\"IngestTelephoneDummyWorker\":{\"id\":\"IngestTelephoneDummyWorker\",\"description\":\"ingest source dataset\",\"level\":1,\"info\":{\"operationType\":\"ingester\"},\"params\":[{\"key\":\"workTime\",\"description\":\"seconds the worker will take\",\"valuetype\":\"INT\",\"required\":true,\"isMultivalued\":false,\"defaultValue\":10,\"possibleValues\":[]}],\"subOperations\":{}},\"IngestPeopleDummyWorker\":{\"id\":\"IngestPeopleDummyWorker\",\"level\":1,\"info\":{\"operationType\":\"ingester\"},\"params\":[],\"subOperations\":{}}}}]";
    private List<Operation> operations;
    private Map<DataOperationReceipt, Integer> map = new HashMap<>();

    @Override
    public List<String> getSelectorTypes()
    {
        return Lists.newArrayList("MSISDN", "IMSI", "Social Media Handle");
    }

    @Override
    public List<String> getDataSources(){
        return Lists.newArrayList("Facebook", "DeviantArt", "Reddit", "MeleeItOnMe");
    }

    public DemoDataEngineClient()
    {
        Type listType = new TypeToken<ArrayList<Operation>>(){}.getType();
        this.operations = new Gson().fromJson(listOperationJson, listType);
    }

    @Override
    public List<Operation> listOperations() {
        return this.operations;

    }

    @Override
    public DataOperationReceipt startNetworkBuild(String sessionID, String username, List<String> dataFilePaths, Map<String, Object> params) throws Exception {
        DataOperationReceipt r = new DataOperationReceipt(sessionID, "TEST");
        this.map.put(r, 0);
        return r;
    }

    @Override
    public boolean networkBuildIsDone(DataOperationReceipt receipt) {
        int count = this.map.get(receipt);
        if (count > 2)
        {
            return true;
        }
        else {
            count = count + 1;
            this.map.put(receipt, count);
            return false;
        }
    }
}
