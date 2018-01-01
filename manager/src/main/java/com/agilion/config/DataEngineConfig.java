package com.agilion.config;

import com.agilion.domain.mock.MockDataEngineClient;
import com.agilion.services.dataengine.DataEngineClient;
import dataengine.ApiClient;
import dataengine.api.DatasetsApi;
import dataengine.api.JobsApi;
import dataengine.api.RequestsApi;
import dataengine.api.SessionsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
@Configuration
public class DataEngineConfig
{
    @Value("${com.agilion.manager.dataengine.endpoint}")
    private String dataEngineRootEndpoint;

    @Bean
    public ApiClient apiClient()
    {
        // Create a new Api Client that will be used by all of the Data Engine API clients
        ApiClient client = new ApiClient();
        client.setBasePath(dataEngineRootEndpoint);

        // Set the api client as the default
        dataengine.Configuration.setDefaultApiClient(client);
        return client;
    }

    @Bean
    public SessionsApi sessionsApi()
    {
        SessionsApi sessionApi = new SessionsApi(apiClient());
        return sessionApi;
    }

    @Bean
    public JobsApi jobsApi()
    {
        JobsApi jobsApi = new JobsApi(apiClient());
        return jobsApi;
    }

    @Bean
    public DatasetsApi datasetsApi()
    {
        DatasetsApi sessionApi = new DatasetsApi(apiClient());
        return sessionApi;
    }

    @Bean
    public RequestsApi requestsApi()
    {
        RequestsApi requestsApi = new RequestsApi(apiClient());
        return requestsApi;
    }

    @Bean
    public DataEngineClient client()
    {
        return new MockDataEngineClient();
    }
}
