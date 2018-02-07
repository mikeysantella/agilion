package dataengine.api;

import dataengine.api.*;
import dataengine.api.RequestApiService;
import dataengine.api.factories.RequestApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import dataengine.api.Request;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/request")


@io.swagger.annotations.Api(description = "the request API")

public class RequestApi  {
   private final RequestApiService delegate = RequestApiServiceFactory.getRequestApi();

    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve request", notes = "get request based on id parameter", response = Request.class, tags={ "requests", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "request object", response = Request.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad id parameter", response = Request.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "request not found", response = Request.class) })
    public Response getRequest(@ApiParam(value = "request ID",required=true) @PathParam("id") String id
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getRequest(id,securityContext);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "submit a request", notes = "Submit a new request with given metadata", response = Request.class, tags={ "requests", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "request created", response = Request.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "invalid input, object invalid", response = Request.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "an existing request already exists", response = Request.class) })
    public Response submitRequest(@ApiParam(value = "Request to add" ) Request request
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.submitRequest(request,securityContext);
    }
}
