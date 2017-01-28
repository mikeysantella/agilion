package dataengine.api.factories;

import dataengine.api.DatasetApiService;
import dataengine.api.impl.DatasetApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class DatasetApiServiceFactory {
    private final static DatasetApiService service = new DatasetApiServiceImpl();

    public static DatasetApiService getDatasetApi() {
        return service;
    }
}
