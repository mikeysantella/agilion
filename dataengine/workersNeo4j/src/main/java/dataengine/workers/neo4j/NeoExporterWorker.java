package dataengine.workers.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.OperationParam;
import dataengine.api.OperationParam.ValuetypeEnum;
import dataengine.apis.OperationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.workers.BaseWorker;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.PropertiesUtil;

@Slf4j
@Accessors(fluent = true)
public class NeoExporterWorker extends BaseWorker<Job> {
  
  public static void main(String[] args) throws Exception {
    NeoExporterWorker worker = new NeoExporterWorker(null);
//    worker.init("tide.props");
    Map<String, Object> params=new HashMap<>();
//    
//    Map<String, String> nodeInputs=new HashMap<>();
//    String mysqlDir="/home/dlam/dev/agilionReal/dataengine/dataio/mysql";
//    nodeInputs.put(new File(mysqlDir,"persons.csv").toURI().toString(), "Person");
//    nodeInputs.put(new File(mysqlDir,"countries.csv").toURI().toString(), "Country");
//    params.put(OperationConsts.CSVFILE2NODELABEL_MAP, nodeInputs);
//    
//    Map<String, String> edgeInputs=new HashMap<>();
//    edgeInputs.put(new File(mysqlDir,"countries.csv").toURI().toString(), "CITIZEN_OF");
//    params.put(OperationConsts.CSVFILE2EDGELABEL_MAP, edgeInputs);

    params.put((OperationConsts.DB_PATH), "/home/dlam/dev/agilionReal/dataengine/main/neoDBs/1519081032039/");
    if (!false) {
      params.put((OperationConsts.EXPORT_PATH), "export.graphml");
      params.put((OperationConsts.EXPORT_FORMAT), ExportFormats.graphml.name());
    } else if (!true) {
      params.put((OperationConsts.EXPORT_PATH), "export.csv");
      params.put((OperationConsts.EXPORT_FORMAT), ExportFormats.csv.name());
    } else if (!true) {
      params.put((OperationConsts.EXPORT_PATH), "export.edgelist");
      params.put((OperationConsts.EXPORT_FORMAT), ExportFormats.edgelist.name());
    } else if (true) {
      params.put((OperationConsts.EXPORT_PATH), "export.nodelist");
      params.put((OperationConsts.PROPERTY_COLUMNS), "id, name");
      params.put((OperationConsts.EXPORT_FORMAT), ExportFormats.nodelist.name());
    }
    Job job = new Job().id("testJob").params(params);
    worker.doWork(job);
  }

  @Inject
  public NeoExporterWorker(RpcClientProvider<SessionsDB_I> sessDb){
    super(OperationConsts.TYPE_EXPORTER, sessDb);
  }

  public void init(String propFile) throws IOException {
    PropertiesUtil.loadProperties(propFile, domainProps);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_EXPORTER);
    return new Operation().level(1).id("NeoExporterWorker").description("").info(info)
        .addParamsItem(new OperationParam().key(OperationConsts.DB_PATH).required(true)
            .description("path to source Neo4j DB")
            .valuetype(ValuetypeEnum.STRING))
        .addParamsItem(new OperationParam().key(OperationConsts.EXPORT_PATH).required(true)
            .description("path of output file")
            .valuetype(ValuetypeEnum.STRING))
        .addParamsItem(new OperationParam().key(OperationConsts.EXPORT_FORMAT).required(true)
            .description("export format") //
            .valuetype(ValuetypeEnum.STRING)
            .defaultValue(ExportFormats.graphml.name()) //
            .addPossibleValuesItem(ExportFormats.graphml) //
            .addPossibleValuesItem(ExportFormats.csv) //
            .addPossibleValuesItem(ExportFormats.nodelist) //
            .addPossibleValuesItem(ExportFormats.edgelist) //
            )
        .addParamsItem(new OperationParam().key(OperationConsts.CYPHER_EXPR)
            .description("expression to execute for 'cypher' export format") //
            .valuetype(ValuetypeEnum.STRING)
            )
        .addParamsItem(new OperationParam().key(OperationConsts.PROPERTY_COLUMNS)
            .description("properties to include as columns in CSV nodelist file") //
            .valuetype(ValuetypeEnum.STRING)
            .defaultValue("id, name")
            );
  }

  enum ExportFormats { graphml, csv, nodelist, edgelist }
  Properties domainProps = new Properties();

  @Override
  public boolean canDo(Job job) {
    return "NeoExporterWorker".equals(job.getParams().get(OperationConsts.WORKER_NAME));
  }
  
  @Override
  protected boolean doWork(Job job) throws Exception {
    String dbPath = (String) job.getParams().get(OperationConsts.DB_PATH);
    String filepath=(String) job.getParams().get(OperationConsts.EXPORT_PATH);
    String cypherExpr=(String) job.getParams().get(OperationConsts.CYPHER_EXPR);
    ExportFormats exportFormat = ExportFormats.valueOf((String) job.getParams().get(OperationConsts.EXPORT_FORMAT));
    File databaseDirectory = new File(dbPath);
    String neoConfig = null;
    try (EmbeddedNeo4j db = EmbeddedNeo4j.builder(databaseDirectory).configFile(neoConfig).readOnly(true)
        .config("apoc.import.file.enabled", "true") //
        .config("apoc.export.file.enabled", "true").create()) {
      state.setPercent(5).setMessage("Created "+db);
      //db.printCypherResult("MATCH (n) RETURN count(*)");
      
      String procCall;
      String config="{}";
      switch(exportFormat) {
        case csv:
          db.registerProcedure(apoc.export.csv.ExportCSV.class);
          if(cypherExpr!=null && cypherExpr.trim().length()>0)
            procCall="apoc.export.csv.query";
          else 
            procCall="apoc.export.csv.all";
          break;
        case nodelist:
          db.registerProcedure(apoc.export.csv.ExportCSV.class);
          procCall="apoc.export.csv.query";
          if(cypherExpr!=null && cypherExpr.trim().length()>0)
            log.warn("Ignoring {}: {}", OperationConsts.CYPHER_EXPR, cypherExpr);
          String pCols=(String) job.getParams().get(OperationConsts.PROPERTY_COLUMNS);
          String returnStr=null;
          for(String col:PropertiesUtil.splitList(",", pCols)) {
            if(returnStr==null)
              returnStr="n."+col;
            else
              returnStr+=", n."+col;
          }
          cypherExpr="MATCH node = (n) RETURN "+returnStr;
          break;
        case edgelist:
          db.registerProcedure(apoc.export.csv.ExportCSV.class);
          procCall="apoc.export.csv.query";
          if(cypherExpr!=null && cypherExpr.trim().length()>0)
            log.warn("Ignoring {}: {}", OperationConsts.CYPHER_EXPR, cypherExpr);
          cypherExpr="MATCH edge = (start)-[]->(end) RETURN start.id,end.id";
          break;
        default:
        case graphml:
          // https://dzone.com/articles/apoc-database-integration-import-and-export-with-a
          db.registerProcedure(apoc.export.graphml.ExportGraphML.class);
          if(cypherExpr!=null && cypherExpr.trim().length()>0)
            procCall="apoc.export.graphml.query";
          else 
            procCall="apoc.export.graphml.all";
          break;
      }
      String cypherCmd;
      if(cypherExpr==null)
        cypherCmd= "CALL "+procCall+"('"+filepath+"',"+config+")";
      else
        cypherCmd= "CALL "+procCall+"('"+cypherExpr+"','"+filepath+"',"+config+")";
      db.printCypherResult(cypherCmd);
    }
    state.setPercent(99).setMessage("Imported CSV files: "+job.getId());
    return true;
  }

}
