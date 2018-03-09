package com.agilion.services.dataengine;

import com.agilion.domain.app.User;
import dataengine.ApiException;
import dataengine.api.*;
import jersey.repackaged.com.google.common.collect.Lists;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LocalDataEngineClient implements DataEngineClient
{
    private SessionsApi sessionsApi;

    private RequestsApi requestApi;

    public LocalDataEngineClient(SessionsApi sessionsApi, RequestsApi requestApi)
    {
        this.sessionsApi = sessionsApi;
        this.requestApi = requestApi;
    }

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
    public Session startSession(String uniqueSessionID, User user) throws ApiException
    {
        Session session = new Session();
        session.setCreatedTime(OffsetDateTime.now());
        session.setId(uniqueSessionID);
        session.setUsername(user.getUsername());
        session.setLabel(user.getUsername()+"-"+uniqueSessionID);

        this.sessionsApi.createSession(session);
        return session;
    }

    @Override
    public Request sendDataEngineOperationRequest(Session session, OperationSelection dataIngestOperation) throws ApiException {
        Request request = new Request();
        String requestID = UUID.randomUUID().toString();
        request.setId(requestID);
        request.setCreatedTime(OffsetDateTime.now());
        request.setSessionId(session.getId());
        request.setOperation(dataIngestOperation);

        this.requestApi.submitRequest(request);

        return request;
    }

    @Override
    public OperationSelection createDataIngestOperations(String inputUri, String dataFormat, boolean hasHeader) throws ApiException {
        OperationSelectionMap suboperationSelections = new OperationSelectionMap();
        Map<String, Object> subopParams = new HashMap<>();
        subopParams.put("inputUri", inputUri);
        subopParams.put("dataSchema", dataFormat);
        subopParams.put("hasHeader", hasHeader);

        OperationSelection subop = new OperationSelection().id("PythonIngestToSqlWorker").params(subopParams);
        suboperationSelections.put(subop.getId(), subop);

        // Add all other data ingest params specified by the user
        Map<String, Object> params = new HashMap<>();
        params.put("ingesterWorker", subop.getId());
        params.put("datasetLabel", UUID.randomUUID().toString());

        // Build the final operation representing the Data Ingest job
        return new OperationSelection().id("AddSourceDataset")
                .params(params).subOperationSelections(suboperationSelections);
    }

    @Override
    public Request getUpdatedRequest(Request request) throws ApiException {
        return this.requestApi.getRequest(request.getId());
    }
}
