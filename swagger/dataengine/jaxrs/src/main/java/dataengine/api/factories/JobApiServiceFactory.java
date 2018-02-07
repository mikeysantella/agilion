package dataengine.api.factories;

import dataengine.api.JobApiService;
import dataengine.api.impl.JobApiServiceImpl;


public class JobApiServiceFactory {
    private final static JobApiService service = new JobApiServiceImpl();

    public static JobApiService getJobApi() {
        return service;
    }
}
