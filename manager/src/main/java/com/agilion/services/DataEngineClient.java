package com.agilion.services;

import dataengine.api.DatasetsApi;
import dataengine.api.JobsApi;
import dataengine.api.SessionsApi;
import org.springframework.stereotype.Service;

/**
 * Created by Alex_Lappy_486 on 2/26/17.
 */
public class DataEngineClient
{
    private SessionsApi sessionApi;

    private DatasetsApi datasetsApi;

    private JobsApi jobsApi;

    public DataEngineClient(String basePath)
    {
        this.sessionApi = new SessionsApi();
        this.datasetsApi = new DatasetsApi();
        this.jobsApi = new JobsApi();

        this.sessionApi.getApiClient().setBasePath(basePath);
        this.datasetsApi.getApiClient().setBasePath(basePath);
        this.jobsApi.getApiClient().setBasePath(basePath);
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
}
