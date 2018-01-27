package com.agilion.services.dataengine.interfaceV2;

import com.agilion.services.dataengine.DataOperationReceipt;
import dataengine.ApiException;
import dataengine.api.*;
import org.springframework.security.access.method.P;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alex_Lappy_486 on 1/26/18.
 */
public class SimpleDataEngineClient implements DataEngineInterface
{
    private SessionsApi sessionsApi;
    private RequestsApi requestApi;

    public SimpleDataEngineClient(SessionsApi sessionsApi, RequestsApi requestApi)
    {
        this.sessionsApi = sessionsApi;
        this.requestApi = requestApi;
    }

    @Override
    public DataOperationReceipt submitDataEngineOperation(OperationSelection operation, String username) {
        String uuid = UUID.randomUUID().toString();
        DataOperationReceipt receipt = null;
        try {
            Session sess = startSession(uuid, username);
            Request req = createDataEngineRequest(sess.getId(), operation);
            this.requestApi.submitRequest(req);
            receipt = new DataOperationReceipt(sess.getId(), req.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return receipt;
    }

    @Override
    public DataOperationStatus getDataEngineOperationStatus(DataOperationReceipt receipt)
    {
        DataOperationStatus status = null;
        try {
            List<Request> requests = this.sessionsApi.getSession(receipt.getSessionID()).getRequests();
            status = new DataOperationStatus(requests.get(0).getState());
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return status;
    }

    private State deriveOverallStatus(List<Request> requests)
    {
        return requests.get(0).getState();
    }

    private Session startSession(String uniqueSessionID, String username) throws ApiException
    {
        Session session = new Session();
        session.setCreatedTime(OffsetDateTime.now());
        session.setId(uniqueSessionID);
        session.setUsername(username);

        this.sessionsApi.createSession(session);
        return session;
    }

    private Request createDataEngineRequest(String sessionID, OperationSelection operation)
    {
        Request request = new Request();
        String requestID = UUID.randomUUID().toString();
        request.setId(requestID);
        request.setCreatedTime(OffsetDateTime.now());
        request.setSessionId(sessionID);
        request.setOperation(operation);

        return request;
    }
}
