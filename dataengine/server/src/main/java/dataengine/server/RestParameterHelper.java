package dataengine.server;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import dataengine.api.ApiResponseMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RestParameterHelper {
  //TODO: 5: makeResponseIfNotSecure
  static Response makeResponseIfNotSecure(SecurityContext securityContext) {
    return null;
  }

  static Response makeBadRequestResponse(String msg) {
    log.warn("Bad request: {}", msg);
    return Response.status(Status.BAD_REQUEST)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, msg))
        .build();
  }

  static Response makeResponseIfIdInvalid(String idType, String id) {
    if (!isValidIdString(id))
      return makeBadRequestResponse("ID for " + idType + " is not valid: " + id + " allowedChars="+allowedChars);
    return null;
  }

  private static String allowedChars="a-zA-Z0-9.-";
  private static Pattern illegalCharsRegex = Pattern.compile("[^"+allowedChars+"]");
  private static boolean isValidIdString(String id) {
    return !illegalCharsRegex.matcher(id).find();
  }


  static Response makeResultResponse(String msg, Future<?> responseObj) {
    try {
      if (responseObj.get() == null) {
        String errMsg = "Null response: " + msg;
        log.warn(errMsg);
        return Response.status(Status.NOT_FOUND)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                errMsg))
            .build();
      } else
        return Response.ok().entity(responseObj.get()).build();
    } catch (InterruptedException | ExecutionException e) {
      String errMsg = "Exception caught during " + msg + ": " + e.getCause();
      log.warn(errMsg, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg))
          .build();
    }
  }

  static Response makeResultResponse(String objectType, String relativeLocationUriPrefix, String id,
      Future<?> responseObj) {
    try {
      Object result = responseObj.get();
      if (result == null) {
        String errMsg = "Object of type " + objectType + " not found with id: " + id;
        log.warn(errMsg);
        return Response.status(Status.NOT_FOUND)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                errMsg))
            .build();
      } else
        return Response.ok(result)
            .location(URI.create(relativeLocationUriPrefix + id))
            .build();
    } catch (InterruptedException | ExecutionException e) {
      String errMsg = objectType + " object, id=" + id + " caused exception: " + e.getCause();
      log.warn(errMsg);
      return Response.status(Status.CONFLICT)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg))
          .build();
    }
  }

  static <T> Response tryCreateObject(String objectType, T inputObj, String relativeLocationUriPrefix,
      Function<T, String> getId, Function<String, Future<Boolean>> hasObject, Supplier<Future<T>> createObject) {
    if (inputObj == null) {
      String errMsg = "Submitted " + objectType + " cannot be null!";
      log.warn(errMsg);
      return makeBadRequestResponse(errMsg);
    }
    try {
      String id = getId.apply(inputObj);
      if (id != null && hasObject.apply(id).get()) {
        String errMsg = "Object already exists of type " + objectType + ": " + id;
        log.warn(errMsg);
        return Response.status(Status.CONFLICT)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg))
            .build();
      }
      Future<T> createF = createObject.get();
      T newObj = createF.get(); // blocks until object created
      if (newObj == null) {
        String errMsg = "Object of type " + objectType + " was not created with id=" + id;
        log.warn(errMsg);
        return Response.status(Status.NO_CONTENT)
            .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg))
            .build();
      }
      return Response
          .created(URI.create(relativeLocationUriPrefix + getId.apply(newObj)))
          .status(Status.CREATED).entity(newObj).build();
    } catch (Exception e) { // could be Vertx msg timeout
      String errMsg = objectType + " object cannot be created from " + inputObj + " : " + e.getCause();
      log.warn(errMsg);
      return Response.status(Status.CONFLICT)
          .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, errMsg))
          .build();
    }
  }
}
