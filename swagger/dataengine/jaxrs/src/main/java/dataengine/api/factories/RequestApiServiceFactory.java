package dataengine.api.factories;

import dataengine.api.RequestApiService;
import dataengine.api.impl.RequestApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class RequestApiServiceFactory {
    private final static RequestApiService service = new RequestApiServiceImpl();

    public static RequestApiService getRequestApi() {
        return service;
    }
}
