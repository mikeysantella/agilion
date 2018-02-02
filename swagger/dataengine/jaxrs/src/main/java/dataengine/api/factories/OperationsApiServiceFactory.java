package dataengine.api.factories;

import dataengine.api.OperationsApiService;
import dataengine.api.impl.OperationsApiServiceImpl;


public class OperationsApiServiceFactory {
    private final static OperationsApiService service = new OperationsApiServiceImpl();

    public static OperationsApiService getOperationsApi() {
        return service;
    }
}
