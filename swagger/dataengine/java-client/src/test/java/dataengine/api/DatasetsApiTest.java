package dataengine.api;

import dataengine.ApiException;
import dataengine.api.Dataset;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for DatasetsApi
 */
public class DatasetsApiTest {

    private final DatasetsApi api = new DatasetsApi();

    
    /**
     * retrieve dataset
     *
     * get dataset based on id parameter
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void getDatasetTest() throws ApiException {
        String id = null;
        // Dataset response = api.getDataset(id);

        // TODO: test validations
    }
    
}
