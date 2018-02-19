package dataengine.apis;

public final class OperationConsts {

  public static final String OPERATION_TYPE = "operationType";

  /// operation types values:  
  public static final String TYPE_INGESTER = "ingester";
  public static final String TYPE_POSTINGEST = "post-ingest";

  public static final String TYPE_MERGER = "merger";
  public static final String TYPE_IMPORTER = "importer";
  public static final String TYPE_EXPORTER = "exporter";

  public static final String TYPE_PREREQUEST = "pre-request";
  public static final String TYPE_POSTREQUEST = "post-request";

  /// operation/job parameter keys:
  public static final String DATASET_LABEL = "datasetLabel";
  public static final String DATASET_ID = "datasetId";
  public static final String INPUT_URI = "inputUri";
  public static final String INPUT_NODELIST_URIS = "inputNodelistUris";
  public static final String INPUT_EDGELIST_URIS = "inputEdgelistUris";
  public static final String DATA_FORMAT = "dataFormat";
  public static final String DATA_SCHEMA = "dataSchema";
  public static final String OUTPUT_URI = "outputUri";
  public static final String PREV_JOBID = "prevJobId";
  public static final String HAS_HEADER = "hasHeader";
  
  /// DATA_FORMAT values:
  public static final String DATA_FORMAT_PARQUET = "parquet";
  public static final String DATA_FORMAT_LUCENE = "lucene";
  public static final String DATA_FORMAT_MYSQL = "mysql";
  public static final String DATA_FORMAT_CSV = "csv";

  public static final String WORKER_NAME = "workerName";

  public static final String INGESTER_WORKER = "ingesterWorker";
  public static final String EXPORTER_WORKER = "exporterWorker";

  /// for ingesting to Neo4j
  public static final String DB_PATH = "dbPath";

  /// for exporting from Neo4j
  public static final String EXPORT_FORMAT = "exportFormat";
  public static final String EXPORT_PATH = "exportPath";

  public static final String CYPHER_EXPR = "cypherExpression";

  public static final String PROPERTY_COLUMNS = "propertyColumns";

  /// Dataset property keys
  public static final String EXPORT_CONCEPTS = "exportConcepts";

  //
  public static final String JOBID_OF_OUTPUT_DATASET = "jobIdOfOutputDataset";

  public static final String INPUT_REQUESTID = "inputRequestId";


}
