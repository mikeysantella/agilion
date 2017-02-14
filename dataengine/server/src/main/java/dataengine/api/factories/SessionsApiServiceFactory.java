package dataengine.api.factories;

import dataengine.api.SessionsApiService;
import dataengine.server.DeServerGuiceInjector;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-14T03:54:22.770Z")
public class SessionsApiServiceFactory {
    private final static SessionsApiService service = 
        DeServerGuiceInjector.singleton().getInstance(SessionsApiService.class);

    public static SessionsApiService getSessionsApi() {
        return service;
    }
}
