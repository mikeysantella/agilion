package dataengine.api;

import dataengine.ApiException;
import dataengine.api.Operation;
import dataengine.api.Request;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for RequestsApi
 */
public class RequestsApiTest {

    private final RequestsApi api = new RequestsApi();

    
    /**
     * retrieve request
     *
     * get request based on id parameter
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void getRequestTest() throws ApiException {
        String id = null;
        // Request response = api.getRequest(id);

        // TODO: test validations
    }
    
    /**
     * retrieve list of operations for requests
     *
     * list operations
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void listOperationsTest() throws ApiException {
        // List<Operation> response = api.listOperations();

        // TODO: test validations
    }
    
    /**
     * refresh list of operations
     *
     * refresh operations list from active workers
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void refreshOperationsTest() throws ApiException {
        // Boolean response = api.refreshOperations();

        // TODO: test validations
    }
    
    /**
     * submit a request
     *
     * Submit a new request with given metadata
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void submitRequestTest() throws ApiException {
        Request request = null;
        // Request response = api.submitRequest(request);

        // TODO: test validations
    }
    
}
