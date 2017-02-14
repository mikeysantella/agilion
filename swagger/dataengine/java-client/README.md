# dataengine-client

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>agilion</groupId>
    <artifactId>dataengine-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "agilion:dataengine-client:0.0.1-SNAPSHOT"
```

### Others

At first generate the JAR by executing:

    mvn package

Then manually install the following JARs:

* target/dataengine-client-0.0.1-SNAPSHOT.jar
* target/lib/*.jar

## Getting Started

Please follow the [installation](#installation) instruction and execute the following Java code:

```java

import dataengine.*;
import dataengine.auth.*;
import dataengine.api.*;
import dataengine.api.DatasetsApi;

import java.io.File;
import java.util.*;

public class DatasetsApiExample {

    public static void main(String[] args) {
        
        DatasetsApi apiInstance = new DatasetsApi();
        String id = "id_example"; // String | dataset ID
        try {
            Dataset result = apiInstance.getDataset(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DatasetsApi#getDataset");
            e.printStackTrace();
        }
    }
}

```

## Documentation for API Endpoints

All URIs are relative to *http://virtserver.swaggerhub.com/deelam/DataEngine/0.0.1*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*DatasetsApi* | [**getDataset**](docs/DatasetsApi.md#getDataset) | **GET** /dataset/{id} | retrieve dataset
*JobsApi* | [**getJob**](docs/JobsApi.md#getJob) | **GET** /job/{id} | retrieve job
*RequestsApi* | [**getRequest**](docs/RequestsApi.md#getRequest) | **GET** /request/{id} | retrieve request
*RequestsApi* | [**listOperations**](docs/RequestsApi.md#listOperations) | **GET** /operations | retrieve list of operations for requests
*RequestsApi* | [**submitRequest**](docs/RequestsApi.md#submitRequest) | **POST** /request | submit a request
*SessionsApi* | [**createSession**](docs/SessionsApi.md#createSession) | **POST** /session | create a session
*SessionsApi* | [**getSession**](docs/SessionsApi.md#getSession) | **GET** /session/{id} | retrieve session
*SessionsApi* | [**listSessionIds**](docs/SessionsApi.md#listSessionIds) | **GET** /sessions/ids | retrieve list of session IDs
*SessionsApi* | [**listSessionNames**](docs/SessionsApi.md#listSessionNames) | **GET** /sessions/names | retrieve list of session names
*SessionsApi* | [**listSessions**](docs/SessionsApi.md#listSessions) | **GET** /sessions | retrieve list of sessions
*SessionsApi* | [**setSessionMetadata**](docs/SessionsApi.md#setSessionMetadata) | **PUT** /session/{id} | modify session metadata


## Documentation for Models

 - [Dataset](docs/Dataset.md)
 - [Job](docs/Job.md)
 - [Operation](docs/Operation.md)
 - [OperationParam](docs/OperationParam.md)
 - [Progress](docs/Progress.md)
 - [Request](docs/Request.md)
 - [Session](docs/Session.md)
 - [State](docs/State.md)


## Documentation for Authorization

All endpoints do not require authorization.
Authentication schemes defined for the API:

## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.

## Author

agilion@deelam.net

