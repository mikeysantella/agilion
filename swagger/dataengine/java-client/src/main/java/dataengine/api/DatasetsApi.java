package dataengine.api;

import dataengine.ApiException;
import dataengine.ApiClient;
import dataengine.Configuration;
import dataengine.Pair;

import javax.ws.rs.core.GenericType;

import dataengine.api.Dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DatasetsApi {
  private ApiClient apiClient;

  public DatasetsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DatasetsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * retrieve dataset
   * get dataset based on id parameter
   * @param id dataset ID (required)
   * @return Dataset
   * @throws ApiException if fails to make API call
   */
  public Dataset getDataset(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getDataset");
    }
    
    // create path and map variables
    String localVarPath = "/dataset/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Dataset> localVarReturnType = new GenericType<Dataset>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
