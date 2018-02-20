package dataengine.workers.neo4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.api.proc.CallableProcedure;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmbeddedNeo4j implements Closeable {

  @Accessors(fluent = true, chain = true)
  @RequiredArgsConstructor
  @Setter
  public static class Builder {
    final File databaseDirectory;
    String configFile = null;
    boolean readOnly = false;
    boolean allowApoc = true;
    Properties config=new Properties();
    
    public Builder config(String key, String value){
      config.setProperty(key, value);
      return this;
    }

    public EmbeddedNeo4j create() {
      return new EmbeddedNeo4j(databaseDirectory, configFile, config, readOnly, allowApoc);
    }
  }

  public static Builder builder(File dbDirectory) {
    return new Builder(dbDirectory);
  }

  private final GraphDatabaseService graphDb;

  @SuppressWarnings("deprecation")
  public EmbeddedNeo4j(File databaseDirectory, String pathToConfig, Properties props,
      boolean readOnly, boolean allowApoc) {
    GraphDatabaseBuilder builder =
        new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(databaseDirectory);
    if (pathToConfig != null)
      builder = builder.loadPropertiesFromFile(pathToConfig);
    if (readOnly)
      builder.setConfig(GraphDatabaseSettings.read_only, "true");
    if (allowApoc) {
      // https://neo4j-contrib.github.io/neo4j-apoc-procedures/index33.html
      builder.setConfig(GraphDatabaseSettings.procedure_unrestricted, "apoc.trigger.*,apoc.meta.*");
      builder.setConfig(GraphDatabaseSettings.procedure_whitelist, "apoc.coll.*,apoc.load.*");
      // https://stackoverflow.com/questions/43266933/loading-a-plugin-into-an-embedded-version-of-neo4j-database
    }
    for (Object k : props.keySet()) {
      String key=(String)k;
      builder.setConfig(key, props.getProperty(key));
    }
    graphDb = builder.newGraphDatabase();
    registerShutdownHook();
  }

  @Override
  public void close() throws IOException {
    graphDb.shutdown();
  }

  public void registerProcedure(Class<?>... procedures)
      throws KernelException {
    Procedures proceduresService =
        ((GraphDatabaseAPI) graphDb).getDependencyResolver().resolveDependency(Procedures.class);
    for (Class<?> procedure : procedures) {
      proceduresService.registerProcedure(procedure);
    }
  }

  private void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        close();
      } catch (IOException e) {
        log.error("When shutting down neo4j", e);
      }
    }, "myShutdownNeo"));
  }

  static class GraphActionException extends RuntimeException {
    private static final long serialVersionUID = 201712212353L;

    public GraphActionException(String msg) {
      super(msg);
    }

    public GraphActionException(String msg, Throwable t) {
      super(msg, t);
    }

    public GraphActionException(Throwable t) {
      super(t);
    }
  }

  public void createIndex(String nodeLabel, String propertyName, boolean waitUntilDone,
      int secondsToWait) {
    txn("create index", graph -> {
      Schema schema = graph.schema();
      IndexDefinition indexDefinition =
          schema.indexFor(Label.label(nodeLabel)).on(propertyName).create();
      if (waitUntilDone)
        schema.awaitIndexOnline(indexDefinition, secondsToWait, TimeUnit.SECONDS);
    });
  }

  public void waitUntilIndexesDone(int minutesToWait) {
    txn("wait for schema actions to complete", graph -> {
      Schema schema = graph.schema();
      schema.awaitIndexesOnline(minutesToWait, TimeUnit.MINUTES);
    });
  }

  public void dropIndex(String nodeLabel) {
    txn("drop index", graph -> {
      Label label = Label.label(nodeLabel);
      for (IndexDefinition indexDefinition : graph.schema().getIndexes(label)) {
        indexDefinition.drop();
      }
    });
  }

  public void txn(String msg, Consumer<GraphDatabaseService> action) {
    Transaction tx = graphDb.beginTx();
    try {
      log.info(msg);
      action.accept(graphDb);
      tx.success();
    } catch (Exception e) {
      tx.failure();
      log.error("When " + msg, e);
      throw new GraphActionException(e);
    } finally {
      tx.close();
    }
  }

  public void cypher(String cypherCmd, Consumer<Result> resultHandler) {
    txn("execute Cypher: " + cypherCmd, graph -> {
      try (Result result = graph.execute(cypherCmd)) {
        if (resultHandler != null)
          resultHandler.accept(result);
      }
    });
  }

  public void cypherNoTxn(String cypherCmd, Consumer<Result> resultHandler) {
    try (Result result = graphDb.execute(cypherCmd)) {
      if (resultHandler != null)
        resultHandler.accept(result);
    }
  }
  
  public void printCypherResult(String cypherCmd) {
    cypher(cypherCmd, createResultHandler(cypherCmd));
  }

  public void printCypherNoTxnResult(String cypherCmd) {
    cypherNoTxn(cypherCmd, createResultHandler(cypherCmd));
  }
  
  static Consumer<Result> createResultHandler(String cypherCmd){
    return result -> {
      List<String> columns = result.columns();
      if (false) {
        StringBuilder sb = new StringBuilder(columns.toString());
        sb.append("\n\t");
        while (result.hasNext()) {
          Map<String, Object> row = result.next();
          for (Entry<String, Object> column : row.entrySet()) {
            sb.append(column.getKey() + ": " + column.getValue() + "; ");
          }
          sb.append("\n\t");
        }
        log.info("Result of '{}': \n\t{}", cypherCmd, sb);
      } else
        log.info("Result of '{}': \n{}", cypherCmd, result.resultAsString());
    };
  }
}
