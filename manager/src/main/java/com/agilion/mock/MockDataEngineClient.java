package com.agilion.mock;

import com.agilion.services.dataengine.DataEngineClient;
import dataengine.ApiException;
import dataengine.api.*;
import jersey.repackaged.com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

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
    public void startNetworkBuild(String sessionID, String username, List<String> dataFilePaths, Map<String, Object> params) throws ApiException
    {
        // First, start the session
        Session session = startSession(sessionID, username);

        // Next, create one or more data engine requests for each data file.
        List<Request> requests = new LinkedList<>();
        for (String filepath : dataFilePaths)
        {
           OperationSelection selection = createDataIngestOperation(filepath);
           requests.add(createDataEngineRequest(session, selection));
        }

        // Finally, submit the requests to the data engine.
        for (Request request : requests)
        {
            requestApi.submitRequest(request);
        }
    }

    private Session startSession(String uniqueSessionID, String username) throws ApiException {
        Session session = new Session();
        session.setCreatedTime(OffsetDateTime.now());
        session.setId(uniqueSessionID);
        session.setUsername(username);

        this.sessionsApi.createSession(session);
        return session;
    }

    private Request createDataEngineRequest(Session session, OperationSelection dataIngestOperation)
    {
        Request request = new Request();
        String requestID = UUID.randomUUID().toString();
        request.setId(requestID);
        request.setCreatedTime(OffsetDateTime.now());
        request.setSessionId(session.getId());
        request.setOperation(dataIngestOperation);

        return request;
    }

    private OperationSelection createDataIngestOperation(String filepath)
    {
        // Build parameters of operation based on form values
        Map<String, Object> params = new HashMap<>();
        params.put("inputUri", filepath);
        params.put("dataFormat", "HOW ARE WE DERIVING THE DATA SOURCE NAMES? IMPLICITLY OR EXPLICITLY?"); //TODO

        // Build an ingester worker definition
        OperationSelectionMap suboperationSelections = new OperationSelectionMap();
        Map<String, Object> map = new HashMap<>();
        map.put("workTime", "10");

        OperationSelection subop = new OperationSelection().id("IngestTelephoneDummyWorker").params(map);
        suboperationSelections.put(subop.getId(), subop);

        // Add all other data ingest params specified by the user
        params.put("ingesterWorker", subop.getId());

        // Build the final operation representing the Data Ingest job
        return new OperationSelection().id("AddSourceDataset")
                .params(params).subOperationSelections(suboperationSelections);
    }
}
