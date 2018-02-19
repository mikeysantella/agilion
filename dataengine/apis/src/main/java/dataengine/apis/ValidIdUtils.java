package dataengine.apis;

import java.util.regex.Pattern;
import net.deelam.utils.IdUtils;

public final class ValidIdUtils {
  
  public static final String allowedChars = "a-zA-Z0-9.-";
  
  static final Pattern illegalCharsRegex = Pattern.compile("[^" + allowedChars + "]");

  private ValidIdUtils() {}

  public static boolean isValidIdString(String id) {
    return !illegalCharsRegex.matcher(id).find();
  }

  public static String genDatabaseName(String sessId) {
    return IdUtils.convertToSafeChars(sessId, 20).replace("-", "_");
  }

  public static String makeValid(String str) {
    return IdUtils.convertToSafeChars(str, 255).replace("_", "-");
  }
}
