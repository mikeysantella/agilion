package com.agilion.services.dataengine;

import com.agilion.domain.app.User;
import com.agilion.domain.networkbuilder.datasets.DataSet;
import com.agilion.domain.networkbuilder.datasets.DataSetReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataengine.ApiException;
import dataengine.api.Operation;
import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.api.Session;
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
 *
 * A class that completely mocks any interactions with the data engine
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
    public Session startSession(String uniqueSessionID, User username) throws ApiException {
        Session s = new Session();
        s.setId(uniqueSessionID);
        s.setUsername(username.getUsername());
        return s;
    }

    @Override
    public Request sendDataEngineOperationRequest(Session session, OperationSelection dataIngestOperation) throws ApiException {
        return new Request();
    }

    @Override
    public OperationSelection createDataIngestOperations(String inputUri, String dataFormat, boolean hasHeader) throws ApiException {
        return new OperationSelection();
    }

    @Override
    public Request getUpdatedRequest(Request request) throws ApiException {
        return new Request();
    }
}
