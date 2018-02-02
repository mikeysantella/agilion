package dataengine.api;

import dataengine.api.*;
import dataengine.api.JobApiService;
import dataengine.api.factories.JobApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import dataengine.api.Job;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/job")


@io.swagger.annotations.Api(description = "the job API")

public class JobApi  {
   private final JobApiService delegate = JobApiServiceFactory.getJobApi();

    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "retrieve job", notes = "get job based on id parameter", response = Job.class, tags={ "jobs", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "job object", response = Job.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad id parameter", response = Job.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "job not found", response = Job.class) })
    public Response getJob(@ApiParam(value = "job ID",required=true) @PathParam("id") String id
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getJob(id,securityContext);
    }
}
