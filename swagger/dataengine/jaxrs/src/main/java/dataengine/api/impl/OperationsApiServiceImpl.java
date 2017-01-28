package dataengine.api.impl;

import dataengine.api.*;
import dataengine.api.*;

import com.sun.jersey.multipart.FormDataParam;

import dataengine.api.Operation;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class OperationsApiServiceImpl extends OperationsApiService {
    @Override
    public Response listOperations(SecurityContext securityContext)
    throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
