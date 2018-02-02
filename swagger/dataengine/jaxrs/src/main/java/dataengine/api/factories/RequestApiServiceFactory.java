package dataengine.api.factories;

import dataengine.api.RequestApiService;
import dataengine.api.impl.RequestApiServiceImpl;


public class RequestApiServiceFactory {
    private final static RequestApiService service = new RequestApiServiceImpl();

    public static RequestApiService getRequestApi() {
        return service;
    }
}
