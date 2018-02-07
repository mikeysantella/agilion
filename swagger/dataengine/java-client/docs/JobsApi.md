# JobsApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getJob**](JobsApi.md#getJob) | **GET** /job/{id} | retrieve job


<a name="getJob"></a>
# **getJob**
> Job getJob(id)

retrieve job

get job based on id parameter

### Example
```java
// Import classes:
//import dataengine.ApiException;
//import dataengine.api.JobsApi;


JobsApi apiInstance = new JobsApi();
String id = "id_example"; // String | job ID
try {
    Job result = apiInstance.getJob(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling JobsApi#getJob");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| job ID |

### Return type

[**Job**](Job.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

