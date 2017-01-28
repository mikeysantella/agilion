package dataengine.api.factories;

import dataengine.api.SessionApiService;
import dataengine.api.impl.SessionApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class SessionApiServiceFactory {
    private final static SessionApiService service = new SessionApiServiceImpl();

    public static SessionApiService getSessionApi() {
        return service;
    }
}
