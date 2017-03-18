package com.agilion.services;

import dataengine.api.DatasetsApi;
import dataengine.api.JobsApi;
import dataengine.api.RequestsApi;
import dataengine.api.SessionsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Alex_Lappy_486 on 2/26/17.
 */
@Service
public class DataEngineClient
{
    @Autowired
    private SessionsApi sessionApi;

    @Autowired
    private DatasetsApi datasetsApi;

    @Autowired
    private JobsApi jobsApi;

    @Autowired
    private RequestsApi requestsApi;

    public DataEngineClient()
    {
    }

    public SessionsApi getSessionApi() {
        return sessionApi;
    }

    public void setSessionApi(SessionsApi sessionApi) {
        this.sessionApi = sessionApi;
    }

    public DatasetsApi getDatasetsApi() {
        return datasetsApi;
    }

    public void setDatasetsApi(DatasetsApi datasetsApi) {
        this.datasetsApi = datasetsApi;
    }

    public JobsApi getJobsApi() {
        return jobsApi;
    }

    public void setJobsApi(JobsApi jobsApi) {
        this.jobsApi = jobsApi;
    }

    public RequestsApi getRequestsApi() {
        return requestsApi;
    }

    public void setRequestsApi(RequestsApi requestsApi) {
        this.requestsApi = requestsApi;
    }
}
