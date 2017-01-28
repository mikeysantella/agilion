package dataengine.api;

import dataengine.api.*;
import dataengine.api.DatasetApiService;
import dataengine.api.factories.DatasetApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import com.sun.jersey.multipart.FormDataParam;

import dataengine.api.Dataset;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/dataset")


@io.swagger.annotations.Api(description = "the dataset API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class DatasetApi  {
   private final DatasetApiService delegate = DatasetApiServiceFactory.getDatasetApi();

    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve dataset", notes = "get dataset based on id parameter", response = Dataset.class, tags={ "datasets" })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "dataset object", response = Dataset.class),
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad id parameter", response = Dataset.class),
        @io.swagger.annotations.ApiResponse(code = 404, message = "dataset not found", response = Dataset.class) })
    public Response getDataset(
        @ApiParam(value = "dataset ID",required=true) @PathParam("id") String id,
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getDataset(id,securityContext);
    }
}
