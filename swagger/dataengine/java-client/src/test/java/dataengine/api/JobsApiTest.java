package dataengine.api;

import dataengine.ApiException;
import dataengine.api.Job;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for JobsApi
 */
public class JobsApiTest {

    private final JobsApi api = new JobsApi();

    
    /**
     * retrieve job
     *
     * get job based on id parameter
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void getJobTest() throws ApiException {
        String id = null;
        // Job response = api.getJob(id);

        // TODO: test validations
    }
    
}
