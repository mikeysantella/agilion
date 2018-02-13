package dataengine.apis;

import java.util.regex.Pattern;

public final class ValidIdUtils {
  
  public static final String allowedChars = "a-zA-Z0-9.-";
  
  static final Pattern illegalCharsRegex = Pattern.compile("[^" + allowedChars + "]");

  private ValidIdUtils() {}

  public static boolean isValidIdString(String id) {
    return !illegalCharsRegex.matcher(id).find();
  }
}
