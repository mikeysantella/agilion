package dataengine.api.factories;

import dataengine.api.SessionApiService;
import dataengine.server.DeServerGuiceInjector;

public class SessionApiServiceFactory {
  static{
    System.out.println("--> Static load: "+SessionApiServiceFactory.class);
  }
    private final static SessionApiService service = 
        DeServerGuiceInjector.singleton().getInstance(SessionApiService.class);

    public static SessionApiService getSessionApi() {
        return service;
    }
}
