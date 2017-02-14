# DatasetsApi

All URIs are relative to *http://virtserver.swaggerhub.com/deelam/DataEngine/0.0.1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getDataset**](DatasetsApi.md#getDataset) | **GET** /dataset/{id} | retrieve dataset


<a name="getDataset"></a>
# **getDataset**
> Dataset getDataset(id)

retrieve dataset

get dataset based on id parameter

### Example
```java
// Import classes:
//import dataengine.ApiException;
//import dataengine.api.DatasetsApi;


DatasetsApi apiInstance = new DatasetsApi();
String id = "id_example"; // String | dataset ID
try {
    Dataset result = apiInstance.getDataset(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DatasetsApi#getDataset");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| dataset ID |

### Return type

[**Dataset**](Dataset.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

