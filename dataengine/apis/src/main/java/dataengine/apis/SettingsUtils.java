package dataengine.apis;

import java.util.Properties;

public final class SettingsUtils {
  
  public static int parseMsgPersistenceProperty(String str) {
    switch(str){
      case "PERSISTENT":
      case "true":
      case "1":
      case "on":
        return 2; //should be equal to DeliveryMode.PERSISTENT;
      case "NON_PERSISTENT":
      case "false":
      case "0":
      case "off":
        return 1; //should be equal to DeliveryMode.NON_PERSISTENT;
      default:
        throw new IllegalArgumentException("Unparsable boolean string: "+str);
    }
  }
  
  public static boolean parseBooleanProperty(String str) {
    switch(str){
      case "true":
      case "1":
      case "on":
        return true;
      case "false":
      case "0":
      case "off":
        return false;
      default:
        throw new IllegalArgumentException("Unparsable boolean string: "+str);
    }
  }

  public static int deliveryMode(Properties properties) {
    String msgPersistentStr = System.getProperty(CommunicationConsts.MSG_PERSISTENT_SYSPROP);
    if (msgPersistentStr == null || msgPersistentStr.length()==0) {
      msgPersistentStr=properties.getProperty(CommunicationConsts.MSG_PERSISTENCE);
    }
    if (msgPersistentStr == null || msgPersistentStr.length()==0)
      msgPersistentStr="true";
    return parseMsgPersistenceProperty(msgPersistentStr);
  }
}
