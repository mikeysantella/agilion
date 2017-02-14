package dataengine.api;

import dataengine.ApiException;
import dataengine.api.Session;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for SessionsApi
 */
public class SessionsApiTest {

    private final SessionsApi api = new SessionsApi();

    
    /**
     * create a session
     *
     * Creates new session with given metadata
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void createSessionTest() throws ApiException {
        Session session = null;
        // Session response = api.createSession(session);

        // TODO: test validations
    }
    
    /**
     * retrieve session
     *
     * get session based on id parameter
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void getSessionTest() throws ApiException {
        String id = null;
        // Session response = api.getSession(id);

        // TODO: test validations
    }
    
    /**
     * retrieve list of session IDs
     *
     * list session IDs
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void listSessionIdsTest() throws ApiException {
        // List<String> response = api.listSessionIds();

        // TODO: test validations
    }
    
    /**
     * retrieve list of session names
     *
     * list session names
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void listSessionNamesTest() throws ApiException {
        // Map response = api.listSessionNames();

        // TODO: test validations
    }
    
    /**
     * retrieve list of sessions
     *
     * list sessions
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void listSessionsTest() throws ApiException {
        // List<Session> response = api.listSessions();

        // TODO: test validations
    }
    
    /**
     * modify session metadata
     *
     * 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void setSessionMetadataTest() throws ApiException {
        String id = null;
        Map props = null;
        // Session response = api.setSessionMetadata(id, props);

        // TODO: test validations
    }
    
}
