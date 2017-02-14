package dataengine.api;

import dataengine.api.*;
import dataengine.api.SessionsApiService;
import dataengine.api.factories.SessionsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import java.util.Map;
import dataengine.api.Session;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/sessions")


@io.swagger.annotations.Api(description = "the sessions API")

public class SessionsApi  {
   private final SessionsApiService delegate = SessionsApiServiceFactory.getSessionsApi();

    @GET
    @Path("/ids")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve list of session IDs", notes = "list session IDs", response = String.class, responseContainer = "List", tags={ "sessions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "session ID strings", response = String.class, responseContainer = "List") })
    public Response listSessionIds(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.listSessionIds(securityContext);
    }
    @GET
    @Path("/names")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve list of session names", notes = "list session names", response = Map.class, tags={ "sessions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "map of session names to their ids", response = Map.class) })
    public Response listSessionNames(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.listSessionNames(securityContext);
    }
    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve list of sessions", notes = "list sessions", response = Session.class, responseContainer = "List", tags={ "sessions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "session objects", response = Session.class, responseContainer = "List") })
    public Response listSessions(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.listSessions(securityContext);
    }
}
