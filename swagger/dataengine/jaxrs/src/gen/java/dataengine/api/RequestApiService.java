package dataengine.api;

import dataengine.api.*;
import dataengine.api.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import dataengine.api.Request;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public abstract class RequestApiService {
    public abstract Response getRequest(String id,SecurityContext securityContext) throws NotFoundException;
    public abstract Response submitRequest(Request request,SecurityContext securityContext) throws NotFoundException;
}
