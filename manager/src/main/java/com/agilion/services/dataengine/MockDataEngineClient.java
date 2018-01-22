package com.agilion.services.dataengine;

import com.agilion.services.dataengine.DataEngineClient;
import com.agilion.services.dataengine.NetworkBuildReceipt;
import dataengine.ApiException;
import dataengine.api.*;
import jersey.repackaged.com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

public class MockDataEngineClient implements DataEngineClient
{
    @Autowired
    private SessionsApi sessionsApi;

    @Autowired
    private RequestsApi requestApi;

    public MockDataEngineClient(){}

    public List<String> getSelectorTypes()
    {
        return Lists.newArrayList("MSISDN", "IMSI", "Social Media Handle");
    }

    public List<String> getDataSources(){
        return Lists.newArrayList("Facebook", "DeviantArt", "Reddit", "MeleeItOnMe");
    }

    @Override
    public List<Operation> listOperations() {
        try {
            return this.requestApi.listOperations();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public NetworkBuildReceipt startNetworkBuild(String sessionID, String username, List<String> dataFilePaths, Map<String, Object> params) throws ApiException
    {
        // First, start the session
        Session session = startSession(sessionID, username);

        // Next, create one or more data engine requests for each data file.
        List<Request> requests = new LinkedList<>();
        for (String filepath : dataFilePaths)
        {
           OperationSelection selection = createDataIngestOperation(filepath);
           requests.add(createDataEngineRequest(sessionID, selection));
        }

        // Finally, submit the requests to the data engine.
        for (Request request : requests)
        {
            requestApi.submitRequest(request);
        }

        return new NetworkBuildReceipt(requests);
    }

    @Override
    public boolean networkBuildIsDone(NetworkBuildReceipt receipt) {
        boolean allFinished = true;
        try
        {

            for (Request request : receipt.getRequestsInBuild())
            {
                Request updatedRequest = this.requestApi.getRequest(request.getId());
                if (!requestIsStopped(updatedRequest))
                {
                    allFinished = false;
                    break;
                }
            }
        }
        catch (ApiException e)
        {
            e.printStackTrace();
            allFinished = false;
        }
        return allFinished;
    }

    private Session startSession(String uniqueSessionID, String username) throws ApiException
    {
        this.requestApi.listOperations();
        this.requestApi.refreshOperations();
        Session session = new Session();
        session.setCreatedTime(OffsetDateTime.now());
        session.setId(uniqueSessionID);
        session.setUsername(username);

        this.sessionsApi.createSession(session);
        return session;
    }

    private Request createDataEngineRequest(String sessionID, OperationSelection dataIngestOperation)
    {
        Request request = new Request();
        String requestID = UUID.randomUUID().toString();
        request.setId(requestID);
        request.setCreatedTime(OffsetDateTime.now());
        request.setSessionId(sessionID);
        request.setOperation(dataIngestOperation);

        return request;
    }

    private OperationSelection createDataIngestOperation(String filepath)
    {
        // Build parameters of operation based on form values
        Map<String, Object> params = new HashMap<>();
        URI uri = new File(filepath).toURI();
        params.put("inputUri", uri.toASCIIString());
        params.put("dataFormat", "TELEPHONE.CSV"); //TODO

        // Build an ingester worker definition
        OperationSelectionMap suboperationSelections = new OperationSelectionMap();
        Map<String, Object> map = new HashMap<>();
        map.put("workTime", "10");

        OperationSelection subop = new OperationSelection().id("IngestTelephoneDummyWorker").params(map);
        suboperationSelections.put(subop.getId(), subop);

        // Add all other data ingest params specified by the user
        params.put("ingesterWorker", subop.getId());

        // Build the final operation representing the Data Ingest job
        return new OperationSelection().id("AddSourceDataset") //a constant
                .params(params).subOperationSelections(suboperationSelections);
    }

    private boolean requestIsStopped(Request request)
    {
        State s = request.getState();
        if (s == State.COMPLETED || s == State.FAILED || s == State.CANCELLED)
            return true;
        else
            return false;
    }
}
