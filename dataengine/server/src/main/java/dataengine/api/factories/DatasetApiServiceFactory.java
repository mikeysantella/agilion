package dataengine.api.factories;

import dataengine.api.DatasetApiService;
import dataengine.server.DeServerGuiceInjector;

public class DatasetApiServiceFactory {
    private final static DatasetApiService service = 
        DeServerGuiceInjector.singleton().getInstance(DatasetApiService.class);

    public static DatasetApiService getDatasetApi() {
        return service;
    }
}
