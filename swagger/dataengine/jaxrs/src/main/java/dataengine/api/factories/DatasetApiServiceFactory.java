package dataengine.api.factories;

import dataengine.api.DatasetApiService;
import dataengine.api.impl.DatasetApiServiceImpl;


public class DatasetApiServiceFactory {
    private final static DatasetApiService service = new DatasetApiServiceImpl();

    public static DatasetApiService getDatasetApi() {
        return service;
    }
}
