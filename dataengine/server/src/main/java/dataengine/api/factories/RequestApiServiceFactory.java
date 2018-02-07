package dataengine.api.factories;

import dataengine.api.RequestApiService;
import dataengine.server.DeServerGuiceInjector;

public class RequestApiServiceFactory {
  
  private final static RequestApiService service = 
      DeServerGuiceInjector.singleton().getInstance(RequestApiService.class);

  public static RequestApiService getRequestApi() {
    return service;
  }
}
