package com.agilion.services.dataengine;

import com.agilion.domain.app.User;
import com.agilion.domain.networkbuilder.datasets.DataSet;
import com.agilion.domain.networkbuilder.datasets.DataSetReference;
import dataengine.ApiException;
import dataengine.api.Operation;
import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.api.Session;

import java.util.List;
import java.util.Map;

/**
 * Defines operations for communicating with the data engine. All data engine operations should be done through this interface,
 * (rather than calling the RequestsApi/SessionsApi classes directly).
 */
public interface DataEngineClient
{
    /**
     * Returns all
     * @return
     */
    public List<String> getSelectorTypes();

    public List<String> getDataSources();

    public List<Operation> listOperations();

    public Session startSession(String uniqueSessionID, User username) throws ApiException;

    public Request sendDataEngineOperationRequest(Session session, OperationSelection dataIngestOperation) throws ApiException;;

    public OperationSelection createDataIngestOperations(String inputUri, String dataFormat, boolean hasHeader) throws ApiException;

    public Request getUpdatedRequest(Request request) throws ApiException;
}
