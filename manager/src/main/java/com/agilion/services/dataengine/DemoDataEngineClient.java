package com.agilion.services.dataengine;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataengine.api.Operation;
import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
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

    public DemoDataEngineClient() throws IOException {
        Type listType = new TypeToken<ArrayList<Operation>>(){}.getType();
        String listOperationJson = null;
        ClassPathResource demoListOperations = null;
        try
        {
             demoListOperations = new ClassPathResource("static/demoFiles/sampleListOperations.json");
            listOperationJson = FileUtils.readFileToString(demoListOperations.getFile(),"UTF-8");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

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
