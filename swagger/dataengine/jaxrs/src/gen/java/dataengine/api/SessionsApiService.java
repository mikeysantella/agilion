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


public abstract class SessionsApiService {
    public abstract Response listSessionIds(SecurityContext securityContext) throws NotFoundException;
    public abstract Response listSessionNames(SecurityContext securityContext) throws NotFoundException;
    public abstract Response listSessions(SecurityContext securityContext) throws NotFoundException;
}
