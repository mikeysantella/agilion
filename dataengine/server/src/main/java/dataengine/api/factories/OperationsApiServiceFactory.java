package dataengine.api.factories;

import dataengine.api.OperationsApiService;
import dataengine.server.DeServerGuiceInjector;

public class OperationsApiServiceFactory {
    private final static OperationsApiService service = 
        DeServerGuiceInjector.singleton().getInstance(OperationsApiService.class);

    public static OperationsApiService getOperationsApi() {
        return service;
    }
}
