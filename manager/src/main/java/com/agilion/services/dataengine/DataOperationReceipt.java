package com.agilion.services.dataengine;

import dataengine.api.Request;
import jersey.repackaged.com.google.common.collect.Lists;

import java.util.List;

public class DataOperationReceipt
{
    private String sessionID;
    private List<String> requestIDs;

    public DataOperationReceipt(String sessionID, List<String> requestIDs)
    {
        this.sessionID = sessionID;
        this.requestIDs = requestIDs;
    }

    public DataOperationReceipt(String sessionID, String requestID)
    {
        this(sessionID, Lists.newArrayList(requestID));
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public List<String> getRequestIDs() {
        return requestIDs;
    }

    public void setRequestIDs(List<String> requestIDs) {
        this.requestIDs = requestIDs;
    }
}
