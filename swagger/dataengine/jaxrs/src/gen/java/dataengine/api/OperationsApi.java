package dataengine.api;

import dataengine.api.*;
import dataengine.api.OperationsApiService;
import dataengine.api.factories.OperationsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import dataengine.api.Operation;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/operations")


@io.swagger.annotations.Api(description = "the operations API")

public class OperationsApi  {
   private final OperationsApiService delegate = OperationsApiServiceFactory.getOperationsApi();

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve list of operations for requests", notes = "list operations", response = Operation.class, responseContainer = "List", tags={ "requests", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "operation objects", response = Operation.class, responseContainer = "List") })
    public Response listOperations(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.listOperations(securityContext);
    }
}
