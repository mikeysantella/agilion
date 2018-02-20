package dataengine.workers.neo4j;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class CsvToNeoWorker extends BaseWorker<Job> {
  
  public static void main(String[] args) throws Exception {
    final Properties props = PropertiesUtil.loadProperties("tide.props");
    props.put("tideEdges.neo4jCsvLoadPropFile", "tide.props");
    CsvToNeoWorker worker = new CsvToNeoWorker(null, props);
    System.out.println(worker.initOperation());

    Map<String, Object> params=new HashMap<>();
    String mysqlDir="/home/dlam/dev/agilionReal/dataengine/dataio/mysql";
    params.put((OperationConsts.DATA_SCHEMA), "tideEdges");
    params.put((OperationConsts.DB_PATH), "ingested-neo5");
    params.put("Person", new File(mysqlDir,"persons.csv").toURI().toString());
    params.put("Country", new File(mysqlDir,"countries.csv").toURI().toString());
    params.put("CITIZEN_OF", new File(mysqlDir,"countries.csv").toURI().toString());
    Job job = new Job().id("testJob").params(params);
    worker.doWork(job);
  }
  
  public static class Factory {
    RpcClientProvider<SessionsDB_I> sessDb;
    @Inject
    Factory(RpcClientProvider<SessionsDB_I> sessDb){
      this.sessDb=sessDb;
    }
    public CsvToNeoWorker create(Properties configMap) {
      return new CsvToNeoWorker(sessDb, configMap);
    }
  }

  final Properties configMap;
  CsvToNeoWorker(RpcClientProvider<SessionsDB_I> sessDb, Properties configMap){
    super(OperationConsts.TYPE_IMPORTER, sessDb);
    this.configMap=configMap;
    log.info("CsvToNeoWorker configMap={}", configMap);
  }

  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_IMPORTER);
    ;
    final Operation operation = new Operation().level(1).id(jobType()).description("").info(info);
    operation.addParamsItem(new OperationParam().key(OperationConsts.DB_PATH).required(true)
        .description("path to output Neo4j DB")
        .valuetype(ValuetypeEnum.STRING));
    operation.addParamsItem(new OperationParam().key(OperationConsts.DATA_SCHEMA).required(true)
        .description("determines how CSV files are loaded into Neo4j")
        .valuetype(ValuetypeEnum.STRING));
    
//    for(String nodeLabel:nodeLabels) {
//      OperationParam opParam = new OperationParam().key(nodeLabel)
//          .description("CSV file for node type '"+nodeLabel+"'")
//          .valuetype(ValuetypeEnum.STRING);
//      operation.addParamsItem(opParam);
//    }
//    for(String edgeLabel:edgeLabels) {
//      OperationParam opParam = new OperationParam().key(edgeLabel)
//          .description("CSV file for edge type '"+edgeLabel+"'")
//          .valuetype(ValuetypeEnum.STRING);
//      operation.addParamsItem(opParam);
//    }
    return operation
//        .addParamsItem(new OperationParam().key(OperationConsts.CSVFILE2NODELABEL_MAP).required(true)
//            .description("mapping of node label to nodelist CSV file")
//            .possibleValues((List) ))
//        .addParamsItem(new OperationParam().key(OperationConsts.CSVFILE2EDGELABEL_MAP).required(true)
//            .description("mapping of edge label to edgelist CSV file")
//            .possibleValues((List) PropertiesUtil.splitList(domainProps, "edgeLabels", " ")))
        ;
  }
  
  @Override
  public boolean canDo(Job job) {
    //return OperationConsts.DATA_FORMAT_CSV.equals(job.getParams().get(OperationConsts.DATA_FORMAT)) &&
    return "CsvToNeoWorker".equals(job.getParams().get(OperationConsts.WORKER_NAME));
  }
  
  @Override
  protected boolean doWork(Job job) throws Exception {
    String dataSchema=(String) job.getParams().get(OperationConsts.DATA_SCHEMA);
    String propFile=configMap.getProperty(dataSchema+".neo4jCsvLoadPropFile");
    if(propFile==null)
      throw new IllegalStateException("No setting for "+dataSchema+".neo4jCsvLoadPropFile");
    final Properties domainProps = PropertiesUtil.loadProperties(propFile);
    log.info("{} domainProps={}",propFile, domainProps);
    final List<String> nodeLabels = PropertiesUtil.splitList(domainProps, "nodeLabels", " ");
    final List<String> edgeLabels = PropertiesUtil.splitList(domainProps, "edgeLabels", " ");

    
    String dbPath = (String) job.getParams().get(OperationConsts.DB_PATH);
    File databaseDirectory = new File(dbPath);
    String neoConfig = null;
    log.info("Opening Neo4j DB at {}", dbPath);
    try (EmbeddedNeo4j db = EmbeddedNeo4j.builder(databaseDirectory).configFile(neoConfig).create()) {
      state.setPercent(5).setMessage("Created "+db);
      
      Map<String, String> nodeInputs = extractLabelsToImport(job, nodeLabels);
      log.info("nodeInputs={}", nodeInputs);
      final Collection<String> nodeLabelsToImport = nodeInputs.keySet();
      state.setPercent(10).setMessage("Importing nodes: "+nodeLabelsToImport);
      importNodeCsvFiles(db, domainProps, nodeInputs);
      state.setPercent(50).setMessage("Indexing nodes: "+nodeLabelsToImport);
      createUniqueConstraint(db, domainProps,nodeLabelsToImport);
      createIndices(db, domainProps, nodeLabelsToImport);
      db.waitUntilIndexesDone(10);

      Map<String, String> edgeInputs = extractLabelsToImport(job, edgeLabels);
      log.info("edgeInputs={}", edgeInputs);
      if(nodeInputs.size()==0 && edgeInputs.size()==0)
        log.warn("Empty nodeInputs or edgeInputs -- see INFO logs");
      state.setPercent(60).setMessage("Importing edges: "+edgeInputs.values());
      importEdgeCsvFiles(db, domainProps, edgeInputs);
    }
    state.setPercent(99).setMessage("Imported CSV files: "+job.getId());
    return true;
  }

  public Map<String, String> extractLabelsToImport(Job job, List<String> labels) {
    Map<String, String> labelsToImport =new HashMap<>();
    log.info("labels={}",labels);
    log.info("job.getParams()={}",job.getParams());
    for(String label:labels) {
      String csvFile=(String) job.getParams().get(label);
      if(csvFile!=null && csvFile.trim().length()!=0)
        labelsToImport.put(label, csvFile);
    }
    return labelsToImport;
  }

  private void importNodeCsvFiles(EmbeddedNeo4j db, Properties domainProps, Map<String, String> inputs) {
    for (Entry<String, String> e : inputs.entrySet()) {
      String nodeLabel = e.getKey();
      String csvFile = e.getValue();
      log.info("importing {} nodes from {}", nodeLabel, csvFile);
      String idProps = domainProps.getProperty(nodeLabel + ".idProps", "");
      if (idProps.trim().length() == 0)
        log.warn("No 'idProps' setting defined for nodeLabel={}", nodeLabel);
      String propMapping = domainProps.getProperty(nodeLabel + ".propMapping", "");
      if (propMapping.trim().length() == 0)
        log.warn("No 'propMapping' setting  defined for nodeLabel={}", nodeLabel);
      String cypherCmdSuffix = domainProps.getProperty(nodeLabel + ".cypherCmdSuffix", "");
      // http://neo4j.com/docs/developer-manual/current/cypher/clauses/merge/
      String cypherCmd = "USING PERIODIC COMMIT "
          + "LOAD CSV WITH HEADERS FROM \"" + csvFile + "\" AS r " //
          // TODO: test if CREATE would be faster than MERGE
          // see https://neo4j.com/developer/guide-importing-data-and-etl/#_importing_the_data_using_cypher
          + "MERGE (n:" + nodeLabel + " {" + idProps + "})";
      if (propMapping != null && propMapping.trim().length() > 0) {
        cypherCmd += " ON CREATE SET " + propMapping 
            + " ON MATCH SET " + propMapping;
      }
      cypherCmd += " "+cypherCmdSuffix;
      log.info("cypherCmd={}", cypherCmd);
      db.printCypherNoTxnResult(cypherCmd);
      //db.printCypherResult("MATCH (n) RETURN count(*)");
    }
  }

  private void importEdgeCsvFiles(EmbeddedNeo4j db, Properties domainProps, Map<String, String> inputs) {
    for (Entry<String, String> e : inputs.entrySet()) {
      String edgeLabel = e.getKey();
      String csvFile = e.getValue();
      log.info("importing {} edges from {}", edgeLabel, csvFile);
      String idProps = domainProps.getProperty(edgeLabel + ".idProps", "");
      if (idProps.trim().length() == 0)
        log.warn("No 'idProps' setting defined for edgeLabel={}", edgeLabel);
      String propMapping = domainProps.getProperty(edgeLabel + ".propMapping", "");
      final String fromNode = domainProps.getProperty(edgeLabel + ".fromNode");
      checkNotNull(fromNode, "No 'fromNode' property defined for edgeLabel=" + edgeLabel);
      final String toNode = domainProps.getProperty(edgeLabel + ".toNode");
      checkNotNull(toNode, "No 'toNode' property defined for edgeLabel=" + edgeLabel);
      String cypherCmdSuffix = domainProps.getProperty(edgeLabel + ".cypherCmdSuffix", "");
      // http://neo4j.com/docs/developer-manual/current/cypher/clauses/merge/
      String cypherCmd = "USING PERIODIC COMMIT "
          + "LOAD CSV WITH HEADERS FROM \"" + csvFile + "\" AS r "
          + "MERGE (f:"+fromNode+") " // 
          + "MERGE (t:"+toNode+") " //
          + "MERGE (f)-[n:" + edgeLabel + " {" + idProps + "}]->(t) ";
      if (propMapping != null && propMapping.trim().length() > 0) {
        cypherCmd += " ON CREATE SET " + propMapping 
            + " ON MATCH SET " + propMapping;
      }
      cypherCmd += " "+cypherCmdSuffix;
      log.info("cypherCmd={}", cypherCmd);
      db.printCypherNoTxnResult(cypherCmd);
//      db.printCypherResult("MATCH (n) RETURN count(*)");
//      db.printCypherResult("MATCH ()-[r]->() RETURN count(*)");
    }
  }

  private void createUniqueConstraint(EmbeddedNeo4j db, Properties domainProps, Collection<String> nodeLabels) {
    for (String nodeLabel : nodeLabels)
      for (String uniqProp : PropertiesUtil.splitList(domainProps, nodeLabel + ".uniqueProps", ",")) {
        String cypherCmd = "CREATE CONSTRAINT ON (n:"+nodeLabel+") ASSERT n."+uniqProp+" IS UNIQUE";
        db.cypher(cypherCmd, null);
      }
  }

  private void createIndices(EmbeddedNeo4j db, Properties domainProps, Collection<String> nodeLabels) {
    for (String nodeLabel : nodeLabels)
      for (String propToIndex : PropertiesUtil.splitList(domainProps, nodeLabel + ".propsToIndex", ",")) {
        String cypherCmd = "CREATE INDEX ON :" + nodeLabel + "(" + propToIndex + ")";
        db.cypher(cypherCmd, null);
      }
  }
}
