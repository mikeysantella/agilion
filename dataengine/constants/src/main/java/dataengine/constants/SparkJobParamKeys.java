package dataengine.constants;

public final class SparkJobParamKeys {
  public static final String VERBOSE = "verbose";
  
  public static final String INPUT_FILE = "inputFile";
  public static final String DOMAIN_CLASS = "domainClass";
  public static final String OUTPUT_PATH = "outputPath";

  // tablename from where selectors are extracted
  public static final String DOMAIN_DEVICE_TABLE = "domainDeviceTable";
  
  // path to the latest selectors table
  public static final String CURR_SELECTORSTABLE_PATH = "selectorsTablePath";

  // whether to remove first line from CSV file
  public static final String INPUT_HAS_HEADER = "inputHasHeader";
  
  public static final String CSV_DELIMITER = "csvDelimiter";
  
  // whether to remove first line from CSV file
  public static final String REMOVE_HEADER = "removeHeader";

}
