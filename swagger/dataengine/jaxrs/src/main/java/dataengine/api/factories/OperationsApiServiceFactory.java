package dataengine.api.factories;

import dataengine.api.OperationsApiService;
import dataengine.api.impl.OperationsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class OperationsApiServiceFactory {
    private final static OperationsApiService service = new OperationsApiServiceImpl();

    public static OperationsApiService getOperationsApi() {
        return service;
    }
}
