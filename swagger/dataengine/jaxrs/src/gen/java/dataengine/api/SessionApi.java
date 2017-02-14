package dataengine.api;

import dataengine.api.*;
import dataengine.api.SessionApiService;
import dataengine.api.factories.SessionApiServiceFactory;

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

@Path("/session")


@io.swagger.annotations.Api(description = "the session API")

public class SessionApi  {
   private final SessionApiService delegate = SessionApiServiceFactory.getSessionApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "create a session", notes = "Creates new session with given metadata", response = Session.class, tags={ "sessions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "session created", response = Session.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "invalid input, object invalid", response = Session.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "an existing session already exists", response = Session.class) })
    public Response createSession(@ApiParam(value = "Session to add" ) Session session
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.createSession(session,securityContext);
    }
    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve session", notes = "get session based on id parameter", response = Session.class, tags={ "sessions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "session object", response = Session.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad id parameter", response = Session.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "session not found", response = Session.class) })
    public Response getSession(@ApiParam(value = "session ID",required=true) @PathParam("id") String id
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getSession(id,securityContext);
    }
    @PUT
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "modify session metadata", notes = "", response = Session.class, tags={ "sessions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "updated session object", response = Session.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad id parameter", response = Session.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "session not found", response = Session.class) })
    public Response setSessionMetadata(@ApiParam(value = "session ID",required=true) @PathParam("id") String id
,@ApiParam(value = "metadata to set on session" ,required=true) Map props
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.setSessionMetadata(id,props,securityContext);
    }
}
