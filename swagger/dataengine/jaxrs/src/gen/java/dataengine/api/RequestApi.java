package dataengine.api;

import dataengine.api.*;
import dataengine.api.RequestApiService;
import dataengine.api.factories.RequestApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import com.sun.jersey.multipart.FormDataParam;

import dataengine.api.Request;
import dataengine.api.Session;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/request")


@io.swagger.annotations.Api(description = "the request API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class RequestApi  {
   private final RequestApiService delegate = RequestApiServiceFactory.getRequestApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "create a request", notes = "Create new request with given metadata", response = Request.class, tags={ "requests",  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "request created", response = Request.class),
        @io.swagger.annotations.ApiResponse(code = 400, message = "invalid input, object invalid", response = Request.class),
        @io.swagger.annotations.ApiResponse(code = 409, message = "an existing request already exists", response = Request.class) })
    public Response createRequest(
        @ApiParam(value = "Request to add" ) Request session,
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.createRequest(session,securityContext);
    }
    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve request", notes = "get request based on id parameter", response = Session.class, tags={ "requests" })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "request object", response = Session.class),
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad id parameter", response = Session.class),
        @io.swagger.annotations.ApiResponse(code = 404, message = "request not found", response = Session.class) })
    public Response getRequest(
        @ApiParam(value = "request ID",required=true) @PathParam("id") String id,
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getRequest(id,securityContext);
    }
}
