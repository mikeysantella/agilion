package dataengine.api;

import dataengine.api.*;
import dataengine.api.SessionApiService;
import dataengine.api.factories.SessionApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import com.sun.jersey.multipart.FormDataParam;

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

@Path("/session")


@io.swagger.annotations.Api(description = "the session API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class SessionApi  {
   private final SessionApiService delegate = SessionApiServiceFactory.getSessionApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "create a session", notes = "Creates new session with given metadata", response = Session.class, tags={ "sessions",  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "session created", response = Session.class),
        @io.swagger.annotations.ApiResponse(code = 400, message = "invalid input, object invalid", response = Session.class),
        @io.swagger.annotations.ApiResponse(code = 409, message = "an existing request already exists", response = Session.class) })
    public Response createSession(
        @ApiParam(value = "Session to add" ) Session session,
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.createSession(session,securityContext);
    }
    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve session", notes = "get session based on id parameter", response = Session.class, tags={ "sessions",  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "session object", response = Session.class),
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad id parameter", response = Session.class),
        @io.swagger.annotations.ApiResponse(code = 404, message = "session not found", response = Session.class) })
    public Response getSession(
        @ApiParam(value = "session ID",required=true) @PathParam("id") String id,
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getSession(id,securityContext);
    }
    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve list of sessions", notes = "list sessions", response = Session.class, responseContainer = "List", tags={ "sessions",  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "session objects", response = Session.class, responseContainer = "List") })
    public Response listSessions(
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.listSessions(securityContext);
    }
    @PUT
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "modify session metadata", notes = "", response = Session.class, tags={ "sessions" })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "updated session object", response = Session.class),
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad id parameter", response = Session.class),
        @io.swagger.annotations.ApiResponse(code = 404, message = "session not found", response = Session.class) })
    public Response setSession(
        @ApiParam(value = "session ID",required=true) @PathParam("id") String id,
        @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.setSession(id,securityContext);
    }
}
