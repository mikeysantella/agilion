package net.deelam.spark;

import java.io.{ File, FileNotFoundException }

import com.google.common.base.Preconditions;

import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

object SpJobSubmitter {
  val INPUT_FILES_TO_COPY = "spark.file.artifacts"

  net.deelam.utils.Log4jUtil.loadXml()
  val log = org.slf4j.LoggerFactory.getLogger(this.getClass)
  println(s"log=$log")

  def main(args: Array[String]): Unit = {
    import java.net.URI
    log.info("Starting")
    org.apache.commons.io.FileUtils.deleteDirectory(new java.io.File("build/result"));
    //import collection.mutable.Map
    val config = if (!false)
      SpJobConfig(sparkMaster = "local[*]",
        deployMode = "client",
        filename = "sparkjob-ingestcsv.props")
    else
      new SpJobConfig(
        appNamePrefix = "testSpJobSubmitter",
        sparkMaster = "local[*]",
        deployMode = "client",
        appJar = "../sparkjobs/build/libs/sparkjobs-0.0.3-SNAPSHOT.jar",
        mainClass = "dataengine.sparkjobs.SparkDummyApp")

    config.setInputParams("inputFile", "sparkjob-log4j.xml")
    config.setInputParams("domainClass", "myDomainClass")

    implicit val htConfigs: HadoopConfigs = null
    val destDirUri = URI.create("hdfs://tmp/somepath")
    config.copyOrResolveFiles(destDirUri, overwrite = true)

    if (config.isReady()) {
      val launcher = new SpJobSubmitter(config)
      val appHandleP = launcher.startJob(l => {
        //l.setConf(SparkLauncher.DRIVER_MEMORY, "2g")
      })

      val appHandle = Await.result(appHandleP.future, 30 seconds)
      log.info("Done {}", appHandle.getAppId)
    } else {
      log.error("Spark job not ready!")
    }
  }

  def findSparkHome(sparkHomeDir: String = null): String = {
    var sparkHome = sparkHomeDir;
    if (sparkHome == null) {
      sparkHome = System.getenv.get("SPARK_HOME")
      if (sparkHome != null) {
        log.info("Using environment variable SPARK_HOME={}", sparkHome)
      } else {
        log.debug("No environment variable SPARK_HOME; searching for spark directory...")
        val prefixes: Array[String] =
          Array("spark_home", "spark-2", "spark-SKIP")
        val sparkDirFile: File = net.deelam.utils.FileDirectoryUtil
          .findFirstDirectoryWithPrefix(new File("."), prefixes)
        if (sparkDirFile == null)
          throw new FileNotFoundException(
            "SPARK_HOME directory not found with prefixes: " + prefixes)
        sparkHome = sparkDirFile.getAbsolutePath
        log.info("Found directory to use as SPARK_HOME: {}", sparkHome)
      }
    }
    if (!new File(sparkHome).exists())
      throw new FileNotFoundException("SPARK_HOME not found: " + sparkHome)
    sparkHome
  }

}

class SpJobSubmitter(config: SpJobConfig) {
  // set for launcher
  net.deelam.utils.JavaLoggingUtil.configureJUL("[%4$s] %5$s%6$s%n")
  val log = org.slf4j.LoggerFactory.getLogger(this.getClass)

  val env = new java.util.HashMap[String, String]();
  if (config.usingHadoop()) {
    val hadoopUser: String = config.hadoopUser
    log.info(s"Setting HADOOP_USER_NAME=$hadoopUser");
    env.put("HADOOP_USER_NAME", hadoopUser);

    var yarnConfDir = env.get("YARN_CONF_DIR");
    if (yarnConfDir == null) {
      yarnConfDir = config.yarnConfDir
      log.info(s"Using YARN_CONF_DIR from config: $yarnConfDir");
      env.put("YARN_CONF_DIR", yarnConfDir);
    } else {
      log.info(s"Using YARN_CONF_DIR from environment: $yarnConfDir");
    }
    if (yarnConfDir != null)
      Preconditions.checkState(new File(yarnConfDir).canRead)
  }

  val launcher = new SparkLauncher(env)
    .setSparkHome(new java.io.File(SpJobSubmitter.findSparkHome()).getAbsolutePath)

  if (config.deployMode != null)
    launcher.setDeployMode(config.deployMode)

  launcher.setMaster(config.sparkMaster)
    .setVerbose(config.verbose)
    .setConf("spark.app.name", config.appName)
    .setAppResource(config.appJar)
    .setMainClass(config.mainClass)
    .addAppArgs(config.appArgs: _*);

  config.staticProps.getOrElse(SpJobSubmitter.INPUT_FILES_TO_COPY, "").split(" ")
    .foreach {
      case "" =>
      case file => {
        if (!new java.io.File(file).exists())
          log.error(s"File does not exist: '$file'")
        launcher.addFile(file); // this will cause the file to be copied to each executor
        // Not for hdfs input files, which must be copied by SpJobConfig 
        // and user must set as a config.inputParam.
      }
    }
  config.inputParams.foreach {
    case (key, value) =>
      launcher.setConf("spark." + key, value);
      log.info(s"Launcher.setConf: spark.$key=$value")
  };

  setClasspath(config);

  def setClasspath(config: SpJobConfig): Unit = {
    import java.nio.file._
    import java.net.URI
    import scala.collection.JavaConversions._

    val classpath: StringBuilder = new StringBuilder(".")
    val currDirPath: Path = Paths.get(new File("").toURI())
    config.classpath foreach {
      case (fileKey: String, uri: URI) => {
        // will make jars available (via Driver's http service or hdfs) to each executor's working directory
        launcher.addJar(uri.toString)
        var filename: String = null
        // http://stackoverflow.com/questions/29972880/access-cassandra-from-spark-com-esotericsoftware-kryo-kryoexception-unable-to
        if (config.usingHadoop()) {
          /**
           * for 'yarn-cluster' spark.master mode, make sure the jar is included in launcher.addJar
           *  so that it's copied to the cluster, where the spark driver is running (even for hdfs: file paths)
           *  AND that only the filename (not the path) is used because addJar copies(symlinks) jars to the working directory.
           *  http://spark.apache.org/docs/latest/running-on-yarn.html
           */
          // complains about 'hdfs' provider: Paths.get(uri).getFileName().toString();
          filename = new File(fileKey).getName
          if (filename != uri)
            log.info(s"For 'yarn-cluster' mode, changing classpath from '${uri}' to '${filename}'")
        } else {
          val file: File = new File(uri)
          if (!file.exists()) {
            log.warn("File not found: {}", uri)
          }
          filename = currDirPath.relativize(Paths.get(uri)).toString
        }
        // spark.driver.extraClassPath and spark.executor.extraClassPath must use colons
        classpath.append(":").append(filename)
      }
    }

    /**
     * http://stackoverflow.com/questions/37132559/add-jars-to-a-spark-job-spark-submit
     * - spark.driver.extraClassPath or it's alias --driver-class-path to set extra classpaths on the Master node.
     * - spark.executor.extraClassPath to set extra class path on the Worker nodes.
     * If you want a certain JAR to be effected on both the Master and the Worker, you have to specify these separately in BOTH flags.
     * Use colon separator on Linux.
     *
     * --conf spark.driver.extraClassPath=... or --driver-class-path: These are aliases, doesn't matter which one you choose
     * --conf spark.driver.extraLibraryPath=..., or --driver-library-path ... Same as above, aliases.
     *
     * --conf spark.executor.extraClassPath=...: Use this when you have a dependency which can't be included in an uber JAR (for example, because there are compile time conflicts between library versions) and which you need to load at runtime.
     * --conf spark.executor.extraLibraryPath=... This is passed as the java.library.path option for the JVM. Use this when you need a library path visible to the JVM.
     */
    val classpathStr: String = classpath.toString

    log.debug(SparkLauncher.DRIVER_EXTRA_CLASSPATH) // "spark.driver.extraClassPath":
    // Usually do not use launcher.addSparkArg() as it clears out lists before setting the value
    // However, launcher.setConf(SparkLauncher.DRIVER_EXTRA_CLASSPATH,)
    // does not put jars before spark-assembly.jar:  launcher.setConf(SparkLauncher.DRIVER_EXTRA_CLASSPATH, classpathStr);
    // Use addSparkArgs("--driver-class-path",) to put jars before spark-assembly.jar (esp. for master="local")
    launcher.addSparkArg("--driver-class-path", classpathStr)

    log.debug(SparkLauncher.EXECUTOR_EXTRA_CLASSPATH) //"spark.executor.extraClassPath"
    launcher.setConf(SparkLauncher.EXECUTOR_EXTRA_CLASSPATH, classpathStr)
  }

  /**
   * Any values specified as flags or in the properties file will be passed on to the application and merged with those specified through SparkConf.
   * Properties set directly on the SparkConf take highest precedence, then flags passed to spark-submit or spark-shell,
   * then options in the spark-defaults.conf file
   */

  import JavaFunctionUtil.toConsumer
  config.staticProps.foreach {
    case (key, value) if (value != null) => {
      key match {
        case key if (key.startsWith("spark.")) =>
          log.info(s"Setting conf $key=$value");
          launcher.setConf(key, value);
        case key if (key.startsWith("env.")) =>
          log.error("NEEDED?   Setting System property from " + key + " in spark-specific property file.");
          System.setProperty(key.substring("env.".length()), value);
        case key =>
          log.warn(s"Ignoring property $key=$value  Launcher will only pass keys starting with 'spark.' to be set in SparkConf() of scala app.");
      }
    }
  }

  def startJob(configure: (SparkLauncher) => Unit = (sl) => Unit) = {
    launcher.setConf("spark.ui.enabled", "false")
    configure(launcher);
    val p = Promise[SparkAppHandle]()
    val sparkAppH = launcher.startApplication(new SparkAppHandle.Listener() {
      def infoChanged(handle: SparkAppHandle) = {
        log.info("infoChanged: " + handle.getAppId);
      }
      def stateChanged(handle: SparkAppHandle) = {
        log.info("stateChanged: " + handle.getState);
        if (handle.getState == SparkAppHandle.State.FINISHED){
          net.deelam.utils.JavaLoggingUtil.disableJUL();
          p.success(handle)
        }
      }
    })
    p
  }

}
