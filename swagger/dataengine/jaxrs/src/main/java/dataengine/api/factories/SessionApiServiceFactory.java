package dataengine.api.factories;

import dataengine.api.SessionApiService;
import dataengine.api.impl.SessionApiServiceImpl;


public class SessionApiServiceFactory {
    private final static SessionApiService service = new SessionApiServiceImpl();

    public static SessionApiService getSessionApi() {
        return service;
    }
}
