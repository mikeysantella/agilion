package com.agilion.services.dataengine;

import dataengine.api.Request;

import java.util.List;

public class NetworkBuildReceipt
{
    private List<Request> requestIDs;

    NetworkBuildReceipt(List<Request> requestIDs)
    {
        this.requestIDs = requestIDs;
    }

    List<Request> getRequestsInBuild()
    {
        return this.requestIDs;
    }
}
