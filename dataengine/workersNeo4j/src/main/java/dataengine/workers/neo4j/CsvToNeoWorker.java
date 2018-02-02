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
    CsvToNeoWorker worker = new CsvToNeoWorker(null, PropertiesUtil.loadProperties("tide.props"));
    System.out.println(worker.initOperation());

    Map<String, Object> params=new HashMap<>();
    String mysqlDir="/home/dlam/dev/agilionReal/dataengine/dataio/mysql";
    params.put((OperationConsts.DB_PATH), "ingested-neo2");
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
    public CsvToNeoWorker create(Properties domainProps) {
      return new CsvToNeoWorker(sessDb, domainProps);
    }
  }

  final Properties domainProps;// = new Properties();
  CsvToNeoWorker(RpcClientProvider<SessionsDB_I> sessDb, Properties domainProps){
    super(OperationConsts.TYPE_CONVERTER, sessDb);
    this.domainProps=domainProps;
    nodeLabels = PropertiesUtil.splitList(domainProps, "nodeLabels", " ");
    edgeLabels = PropertiesUtil.splitList(domainProps, "edgeLabels", " ");
  }

  final List<String> nodeLabels;
  final List<String> edgeLabels;
  @Override
  protected Operation initOperation() {
    Map<String, String> info = new HashMap<>();
    info.put(OperationConsts.OPERATION_TYPE, OperationConsts.TYPE_CONVERTER);
    ;
    final Operation operation = new Operation().level(2).id(jobType()).description("").info(info);
    operation.addParamsItem(new OperationParam().key(OperationConsts.DB_PATH).required(true)
        .description("path to output Neo4j DB"));
    
    for(String nodeLabel:nodeLabels) {
      OperationParam opParam = new OperationParam().key(nodeLabel)
          .description("CSV file for node type '"+nodeLabel+"'")
          .valuetype(ValuetypeEnum.STRING);
      operation.addParamsItem(opParam);
    }
    for(String edgeLabel:edgeLabels) {
      OperationParam opParam = new OperationParam().key(edgeLabel)
          .description("CSV file for edge type '"+edgeLabel+"'")
          .valuetype(ValuetypeEnum.STRING);
      operation.addParamsItem(opParam);
    }
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
  protected boolean doWork(Job job) throws Exception {
    String dbPath = (String) job.getParams().get(OperationConsts.DB_PATH);
    File databaseDirectory = new File(dbPath);
    String neoConfig = null;
    try (EmbeddedNeo4j db = EmbeddedNeo4j.builder(databaseDirectory).configFile(neoConfig).create()) {
      state.setPercent(5).setMessage("Created "+db);
      
      
      Map<String, String> nodeInputs = extractLabelsToImport(job, nodeLabels);
      final Collection<String> nodeLabelsToImport = nodeInputs.keySet();
      state.setPercent(10).setMessage("Importing nodes: "+nodeLabelsToImport);
      importNodeCsvFiles(db, nodeInputs);
      state.setPercent(50).setMessage("Indexing nodes: "+nodeLabelsToImport);
      createUniqueConstraint(db, nodeLabelsToImport);
      createIndices(db, nodeLabelsToImport);
      db.waitUntilIndexesDone(10);

      Map<String, String> edgeInputs = extractLabelsToImport(job, edgeLabels);
      state.setPercent(60).setMessage("Importing edges: "+edgeInputs.values());
      importEdgeCsvFiles(db, edgeInputs);
    }
    state.setPercent(99).setMessage("Imported CSV files: "+job.getId());
    return true;
  }

  public Map<String, String> extractLabelsToImport(Job job, List<String> labels) {
    Map<String, String> labelsToImport =new HashMap<>();
    for(String label:labels) {
      String csvFile=(String) job.getParams().get(label);
      if(csvFile!=null && csvFile.trim().length()!=0)
        labelsToImport.put(label, csvFile);
    }
    return labelsToImport;
  }

  private void importNodeCsvFiles(EmbeddedNeo4j db, Map<String, String> inputs) {
    for (Entry<String, String> e : inputs.entrySet()) {
      String nodeLabel = e.getKey();
      String csvFile = e.getValue();
      String propMapping = domainProps.getProperty(nodeLabel + ".propMapping", "");
      if (propMapping.trim().length() == 0)
        log.warn("No properties defined for nodeLabel={}", nodeLabel);
      String cypherCmdSuffix = domainProps.getProperty(nodeLabel + ".cypherCmdSuffix", "");
      String cypherCmd = "LOAD CSV WITH HEADERS FROM \"" + csvFile + "\" AS r " //
          // TODO: test if CREATE would be faster than MERGE
          // see https://neo4j.com/developer/guide-importing-data-and-etl/#_importing_the_data_using_cypher
          + "MERGE (n:" + nodeLabel + " {" + propMapping + "})"
          + cypherCmdSuffix;
      db.printCypherResult(cypherCmd);
    }
  }

  private void importEdgeCsvFiles(EmbeddedNeo4j db, Map<String, String> inputs) {
    for (Entry<String, String> e : inputs.entrySet()) {
      String edgeLabel = e.getKey();
      String csvFile = e.getValue();
      String propMapping = domainProps.getProperty(edgeLabel + ".propMapping", "");
      final String fromNode = domainProps.getProperty(edgeLabel + ".fromNode");
      checkNotNull(fromNode, "No 'fromNode' property defined for edgeLabel=" + edgeLabel);
      final String toNode = domainProps.getProperty(edgeLabel + ".toNode");
      checkNotNull(toNode, "No 'toNode' property defined for edgeLabel=" + edgeLabel);
      String cypherCmdSuffix = domainProps.getProperty(edgeLabel + ".cypherCmdSuffix", "");
      String cypherCmd = "LOAD CSV WITH HEADERS FROM \"" + csvFile + "\" AS r "
          + "MATCH (f:"+fromNode+") " // 
          + "MATCH (t:"+toNode+") " //
          + "MERGE (f)-[:" + edgeLabel + " {" + propMapping + "}]->(t) "
          + cypherCmdSuffix;
      db.cypher(cypherCmd, null);
    }
  }

  private void createUniqueConstraint(EmbeddedNeo4j db, Collection<String> nodeLabels) {
    for (String nodeLabel : nodeLabels)
      for (String uniqProp : PropertiesUtil.splitList(domainProps, nodeLabel + ".uniqueProps", ",")) {
        String cypherCmd = "CREATE CONSTRAINT ON (n:"+nodeLabel+") ASSERT n."+uniqProp+" IS UNIQUE";
        db.cypher(cypherCmd, null);
      }
  }

  private void createIndices(EmbeddedNeo4j db, Collection<String> nodeLabels) {
    for (String nodeLabel : nodeLabels)
      for (String propToIndex : PropertiesUtil.splitList(domainProps, nodeLabel + ".propsToIndex", ",")) {
        String cypherCmd = "CREATE INDEX ON :" + nodeLabel + "(" + propToIndex + ")";
        db.cypher(cypherCmd, null);
      }
  }
}
