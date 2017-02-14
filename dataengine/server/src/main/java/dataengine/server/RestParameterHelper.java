package dataengine.server;

import static dataengine.server.RestParameterHelper.makeBadRequestResponse;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import dataengine.api.ApiResponseMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RestParameterHelper {
  //TODO:makeResponseIfNotSecure
  static Response makeResponseIfNotSecure(SecurityContext securityContext) {
    return null;
  }

  static Response makeBadRequestResponse(String msg) {
    return Response.status(Status.BAD_REQUEST)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, msg))
        .build();
  }

  static Response makeResponseIfIdInvalid(String idType, String id) {
    if (!isValidIdString(id))
      return makeBadRequestResponse("ID for " + idType + " is not valid: " + id);
    return null;
  }

  private static boolean isValidIdString(String id) {
    log.info("TODO: isValidIdString: " + id);
    return true;
  }


  static Response makeResultResponse(String msg, Future<?> responseObj) {
    try {
      if (responseObj.get() == null)
        return Response.status(Status.NOT_FOUND)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                "Null response: " + msg))
            .build();
      else
        return Response.ok().entity(responseObj.get()).build();
    } catch (InterruptedException | ExecutionException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
              "Exception caught during " + msg + ": " + e))
          .build();
    }
  }

  static Response makeResultResponse(String objectType, String relativeLocationUriPrefix, String id, Future<?> responseObj) {
    try {
      Object result = responseObj.get();
      if (result == null)
        return Response.status(Status.NOT_FOUND)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                "Object of type " + objectType + " not found with id: " + id))
            .build();
      else
        return Response.ok(result)
            .location(URI.create(relativeLocationUriPrefix + id))
            .build();
    } catch (InterruptedException | ExecutionException e) {
      return Response.status(Status.CONFLICT)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
              objectType + " object, id=" + id + " caused exception: " + e.getCause()))
          .build();
    }
  }

  static <T> Response tryCreateObject(String objectType, T inputObj, String relativeLocationUriPrefix,
      Function<T, String> getId, Function<String, Future<Boolean>> hasObject, Supplier<Future<T>> createObject) {
    if (inputObj == null)
      return makeBadRequestResponse("Submitted " + objectType + " cannot be null!");
    try {
      String id = getId.apply(inputObj);
      if (id != null)
        try {
          Boolean objectExists = hasObject.apply(id).get();
          if (objectExists)
            return Response.status(Status.CONFLICT)
                .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                    "Object already exists of type " + objectType + ": " + hasObject))
                .build();
        } catch (Exception e) {
          // good, object doesn't already exist
        }
      Future<T> createF = createObject.get();
      T newObj = createF.get(); // blocks until object created
      if (newObj == null)
        return Response.status(Status.NO_CONTENT)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                "Object of type " + objectType + " was not created with id=" + id))
            .build();
      return Response
          .created(URI.create(relativeLocationUriPrefix + getId.apply(newObj)))
          .status(Status.CREATED).entity(newObj).build();
    } catch (Exception e) { // could be Vertx msg timeout
      return Response.status(Status.CONFLICT)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
              objectType + " object cannot be created from " + inputObj + " : " + e))
          .build();
    }
  }
}
