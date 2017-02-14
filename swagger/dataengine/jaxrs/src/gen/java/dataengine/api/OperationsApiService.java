package dataengine.api;

import dataengine.api.*;
import dataengine.api.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import dataengine.api.Operation;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public abstract class OperationsApiService {
    public abstract Response listOperations(SecurityContext securityContext) throws NotFoundException;
}
