package dataengine.api.factories;

import dataengine.api.JobApiService;
import dataengine.server.DeServerGuiceInjector;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-14T03:54:22.770Z")
public class JobApiServiceFactory {
  private final static JobApiService service = 
      DeServerGuiceInjector.singleton().getInstance(JobApiService.class);

    public static JobApiService getJobApi() {
        return service;
    }
}
