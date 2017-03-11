package net.deelam.spark;

import java.io.{ File }
import java.net.URI
import org.apache.commons.configuration._
import scala.collection.JavaConversions._

/* Deploy modes:
 * "client": local? driver
 * "cluster": remote driver (in the cluster) to reduce latency 
 * 
 * http://stackoverflow.com/questions/34391977/spark-submit-does-automatically-upload-the-jar-to-cluster/34516023#34516023:
 * There are two deploy modes that can be used to launch Spark applications on YARN.
 *  
 * In yarn-cluster mode, the Spark driver runs inside an application master process which is managed by YARN on the cluster, 
 * and the client can go away after initiating the application. In yarn-client mode, the driver runs in the client process, 
 * and the application master is only used for requesting resources from YARN.
 * 
 * YARN cluster mode. Spark submit does upload your jars to the cluster. In particular, it puts the jars in HDFS so your 
 * driver can just read from there. As in other deployments, the executors pull the jars from the driver.
 * 
 * In yarn-cluster mode, the driver runs on a different machine than the client, 
 * so SparkContext.addJar won?t work out of the box with files that are local to the client. 
 * To make files on the client available to SparkContext.addJar, include them with the --jars option in the launch command.
 * 
 */
object SpJobConfig {
  val log = org.slf4j.LoggerFactory.getLogger(this.getClass)

  // sparkMaster
  val LOCAL = "local[*]"
  val YARN = "yarn"

  // deployModes
  val CLUSTER = "cluster" // if sparkMaster=yarn, spark driver runs on yarn and logs located on yarn
  val CLIENT = "client" // if sparkMaster=yarn, good for debugging on yarn since we see logs on local client (except for some Hadoop IOExceptions)

  def apply(sparkMaster: String, deployMode: String, filename: String) = {
    val config = new PropertiesConfiguration(filename);

    val dotIndex = filename.lastIndexOf('.');
    val filebasename = if (dotIndex > 1) filename.substring(0, dotIndex);
    else filename

    val appNamePrefix = config.getString("appNamePrefix", filebasename);
    val mainClass = config.getString("mainClass");
    val appJar = config.getString("appJar");
    val appArgs = config.getList("appArgs", new java.util.ArrayList[String]())
      .asInstanceOf[java.util.List[String]]

    // spark-submitter script verbosity
    val verbose = config.getBoolean("submitter.verbose", true);

    val sj = new SpJobConfig(
      appNamePrefix, sparkMaster, deployMode,
      appJar, mainClass,
      appArgs.toSeq,
      config = config,
      verbose = verbose)

    // app logging
    var log4jFile = config.getString("log4jFile", filebasename + "-log4j.xml");
    if (!new File(log4jFile).exists())
      log4jFile = "sparkjob-log4j.xml";
    sj.setLog4j(log4jFile)

    import JavaFunctionUtil.toConsumer
    config.getKeys("spark").forEachRemaining((keyAny: Any) => {
      val key = keyAny.asInstanceOf[String]
      sj.staticProps.addProperty(key, config.getString(key));
      log.info(s"Adding spark conf property: ${key}=${config.getString(key)}");
    });

    //    if(!sj.staticProps.containsKey("spark.driver.userClassPathFirst"))
    //      sj.staticProps.setProperty("spark.driver.userClassPathFirst","true");
    //    if(!sj.staticProps.containsKey("spark.executor.userClassPathFirst"))
    //      sj.staticProps.setProperty("spark.executor.userClassPathFirst","true");

    log.info(s"Read properties from ${filename}: $sj");
    sj
  }

  val hdfsFileCache = collection.mutable.Map[String, org.apache.hadoop.fs.Path]()

}

class SpJobConfig(private val appNamePrefix: String,
    val sparkMaster: String = SpJobConfig.LOCAL,
    val deployMode: String = null,
    val appJar: String, val mainClass: String,
    val appArgs: Seq[String] = Seq(),
    val classpath: collection.mutable.Map[String, URI] = collection.mutable.Map(),
    // properties with prefix 'spark.' from sparkjob-*.props
    val staticProps: Configuration = new BaseConfiguration(),
    // spark job input params
    val inputParams: collection.mutable.Map[String, String] = collection.mutable.Map(),
    private val config: Configuration = new BaseConfiguration(),
    val verbose: Boolean = true // verbosity of spark-submit script
    ) {

  val log = org.slf4j.LoggerFactory.getLogger(this.getClass)

  val staticFilesForJob = collection.mutable.Map[String, URI]()
  val appName = appNamePrefix + System.currentTimeMillis()
  var log4jFile: String = "log4j.xml" // affects spark job logging
  //needed?: HadoopForSparkJobUtil.setHadoopUserName(hadoopUser, env);

  def usingHadoop() = sparkMaster.startsWith("yarn");

  // http://spark.apache.org/docs/latest/running-on-yarn.html
  def setLog4j(log4jFile: String) = {
    this.log4jFile = log4jFile;
    if (log4jFile != null)
      if (!new File(log4jFile).exists())
        log.warn("Log4j file not found: {}", log4jFile);
      else {
        addToFileArtifacts(log4jFile);
        staticProps.addProperty("spark.driver.extraJavaOptions", "-Ddnl.isDriver=true -Dlog4j.configuration=file:./" + log4jFile); // so log4j will load file
        staticProps.addProperty("spark.executor.extraJavaOptions", "-Ddnl.isExecutor=true -Dlog4j.configuration=file:./" + log4jFile); // so log4j will load file
      }
  }

  def addToFileArtifacts(requiredFile: String) =
    staticProps.addProperty(SpJobSubmitter.INPUT_FILES_TO_COPY, requiredFile);

  def setInputParams(key: String, value: String) =
    inputParams.put(key, value);

  def isReady(): Boolean = {
    val isReady = new java.util.concurrent.atomic.AtomicBoolean(true);

    Seq(
      (sparkMaster, ""),
      (appNamePrefix, ""),
      (appJar, ""),
      (mainClass, ""),
      (appName, ""))
      .foreach {
        case (fieldVal, descr) =>
          if ((fieldVal == null) || fieldVal.isEmpty) {
            log.warn(s"Field ${descr} is not set!");
            isReady.set(false);
          }
      }

    if (classpath.isEmpty)
      log.warn("classpath is empty!");

    if (staticFilesForJob.isEmpty)
      log.info("staticFilesForJob is empty.");

    Seq(appJar)
      .foreach { filename =>
        if (!new File(filename).exists()) {
          log.warn(s"File ${filename} does not exist!");
        }
      }

    getRequiredInputParams().foreach(p =>
      if (!inputParams.containsKey(p)) {
        log.warn("Required parameter not set: {}", p);
        isReady.set(false);
      })

    inputParams.foreach {
      case (key, value) =>
        if (value == null) {
          log.warn(s"Parameter value is null for key=${key}");
          isReady.set(false);
        }
    }

    isReady.get
  }

  def getRequiredInputParams(): Seq[String] =
    config.getList("inputParams.required").asInstanceOf[java.util.List[String]];

  var hadoopUser: String = null
  var yarnConfDir: String = null
  /**
   * @param destDirUri location for artifact files and classpath jars
   */
  def copyOrResolveFiles(destDirUri: URI, overwrite: Boolean)(implicit htConfigs: HadoopConfigs) = {
    val list = getOrInferClasspath();
    if (sparkMaster.startsWith("yarn")) {
      htConfigs.loadConfigs();

      hadoopUser = htConfigs.getHadoopConfig().get("env.HADOOP_USER_NAME", "hdfs");
      yarnConfDir = htConfigs.getHadoopConfig().get("YARN_CONF_DIR", "yarn-conf");

      copyFilesToHdfs(htConfigs, overwrite, destDirUri, config.getList("files").asInstanceOf[java.util.List[String]], staticFilesForJob);
      copyFilesToHdfs(htConfigs, overwrite, destDirUri, list, classpath);
    } else if (sparkMaster.startsWith("local")) {
      // if hdfs desired, call copyFilesToHdfs() and copyClasspathToHdfs() after this
      resolveLocalFiles(config.getList("files").asInstanceOf[java.util.List[String]], staticFilesForJob);
      resolveLocalFiles(list, classpath);
    } else {
      throw new UnsupportedOperationException(sparkMaster);
    }
  }
  private def getOrInferClasspath(): Seq[String] = {
    val cpStr = config.getString("classpath");
    val list = if (cpStr == null || cpStr.equalsIgnoreCase("INFER_FROM_APPJAR"))
      net.deelam.utils.ManifestUtil.getClasspathJars(appJar);
    else
      config.getList("classpath");

    //list.add(appJar);
    log.info("classpath={}", list);
    return list.asInstanceOf[java.util.List[String]];
  }
  private def resolveLocalFiles(files: Seq[String], map: collection.mutable.Map[String, URI]) = {
    files.foreach(srcFile => {
      val localFile = new File(srcFile);
      if (!localFile.exists())
        log.warn(s"Classpath file not found: ${localFile.toURI}");
      val existing = map.put(srcFile, localFile.toURI);
      if (existing.isDefined)
        log.warn("Overriding existing {}={} with {}", srcFile, existing, localFile.toURI());
    });
  }

  private def copyFilesToHdfs(htConfigs: HadoopConfigs, overwrite: Boolean,
    destDirUri: URI, files: Seq[String], map: collection.mutable.Map[String, URI]) = {
    val hdfs = htConfigs.getHdfsUtils();
    val destDir = hdfs.ensureDirExists(destDirUri.toString());
    files.foreach(srcFile => {
      val localFile = new File(srcFile);
      try {
        val hdfsCachedPath = SpJobConfig.hdfsFileCache.get(srcFile);
        if (hdfsCachedPath.isDefined && hdfs.exists(hdfsCachedPath.get.toString())) {
          log.info("Using existing file from cached filepath: {}", hdfsCachedPath);
          setHdfsPath(map, srcFile, localFile, hdfsCachedPath.get);
        } else {
          val dstPath =
            try {
              hdfs.uploadFile(localFile, destDir, overwrite);
            } catch { // can occur when overwrite=false
              case e: java.nio.file.FileAlreadyExistsException =>
                log.info(s"Not overwritting existing file: ${destDir} ${localFile.getName}");
                new org.apache.hadoop.fs.Path(hdfs.makeQualified(destDir), localFile.getName());
            }
          setHdfsPath(map, srcFile, localFile, dstPath);
          SpJobConfig.hdfsFileCache.put(srcFile, dstPath);
        }
      } catch {
        case e: java.io.IOException => e.printStackTrace();
      }
    });
  }

  private def setHdfsPath(map: collection.mutable.Map[String, URI], srcFile: String, localFile: File,
    dstPath: org.apache.hadoop.fs.Path) = {
    val existing = map.put(srcFile, dstPath.toUri());
    if (existing != null)
      log.warn("Overriding existing {}={} with {}", srcFile, existing, localFile.toURI());
  }

}
