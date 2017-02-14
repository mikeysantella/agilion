package dataengine.api;

import dataengine.api.*;
import dataengine.api.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.util.Map;
import dataengine.api.Session;

import java.util.List;
import dataengine.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public abstract class SessionApiService {
    public abstract Response createSession(Session session,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getSession(String id,SecurityContext securityContext) throws NotFoundException;
    public abstract Response setSessionMetadata(String id,Map props,SecurityContext securityContext) throws NotFoundException;
}
