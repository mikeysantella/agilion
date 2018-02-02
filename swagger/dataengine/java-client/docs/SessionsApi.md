# SessionsApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createSession**](SessionsApi.md#createSession) | **POST** /session | create a session
[**getSession**](SessionsApi.md#getSession) | **GET** /session/{id} | retrieve session
[**listSessionIds**](SessionsApi.md#listSessionIds) | **GET** /sessions/ids | retrieve list of session IDs
[**listSessionNames**](SessionsApi.md#listSessionNames) | **GET** /sessions/names | retrieve list of session names
[**listSessions**](SessionsApi.md#listSessions) | **GET** /sessions | retrieve list of sessions
[**setSessionMetadata**](SessionsApi.md#setSessionMetadata) | **PUT** /session/{id} | modify session metadata


<a name="createSession"></a>
# **createSession**
> Session createSession(session)

create a session

Creates new session with given metadata

### Example
```java
// Import classes:
//import dataengine.ApiException;
//import dataengine.api.SessionsApi;


SessionsApi apiInstance = new SessionsApi();
Session session = new Session(); // Session | Session to add
try {
    Session result = apiInstance.createSession(session);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SessionsApi#createSession");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **session** | [**Session**](Session.md)| Session to add | [optional]

### Return type

[**Session**](Session.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getSession"></a>
# **getSession**
> Session getSession(id)

retrieve session

get session based on id parameter

### Example
```java
// Import classes:
//import dataengine.ApiException;
//import dataengine.api.SessionsApi;


SessionsApi apiInstance = new SessionsApi();
String id = "id_example"; // String | session ID
try {
    Session result = apiInstance.getSession(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SessionsApi#getSession");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| session ID |

### Return type

[**Session**](Session.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="listSessionIds"></a>
# **listSessionIds**
> List&lt;String&gt; listSessionIds()

retrieve list of session IDs

list session IDs

### Example
```java
// Import classes:
//import dataengine.ApiException;
//import dataengine.api.SessionsApi;


SessionsApi apiInstance = new SessionsApi();
try {
    List<String> result = apiInstance.listSessionIds();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SessionsApi#listSessionIds");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;String&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="listSessionNames"></a>
# **listSessionNames**
> Map listSessionNames()

retrieve list of session names

list session names

### Example
```java
// Import classes:
//import dataengine.ApiException;
//import dataengine.api.SessionsApi;


SessionsApi apiInstance = new SessionsApi();
try {
    Map result = apiInstance.listSessionNames();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SessionsApi#listSessionNames");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Map**](Map.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="listSessions"></a>
# **listSessions**
> List&lt;Session&gt; listSessions()

retrieve list of sessions

list sessions

### Example
```java
// Import classes:
//import dataengine.ApiException;
//import dataengine.api.SessionsApi;


SessionsApi apiInstance = new SessionsApi();
try {
    List<Session> result = apiInstance.listSessions();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SessionsApi#listSessions");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;Session&gt;**](Session.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="setSessionMetadata"></a>
# **setSessionMetadata**
> Session setSessionMetadata(id, props)

modify session metadata

### Example
```java
// Import classes:
//import dataengine.ApiException;
//import dataengine.api.SessionsApi;


SessionsApi apiInstance = new SessionsApi();
String id = "id_example"; // String | session ID
Map props = new Map(); // Map | metadata to set on session
try {
    Session result = apiInstance.setSessionMetadata(id, props);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SessionsApi#setSessionMetadata");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| session ID |
 **props** | [**Map**](Map.md)| metadata to set on session |

### Return type

[**Session**](Session.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

