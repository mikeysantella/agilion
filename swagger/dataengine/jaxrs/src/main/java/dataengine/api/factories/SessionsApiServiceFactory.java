package dataengine.api.factories;

import dataengine.api.SessionsApiService;
import dataengine.api.impl.SessionsApiServiceImpl;


public class SessionsApiServiceFactory {
    private final static SessionsApiService service = new SessionsApiServiceImpl();

    public static SessionsApiService getSessionsApi() {
        return service;
    }
}
