package dataengine.sparkjobs

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.io.File
import dataengine.constants.SparkJobParamKeys

object SparkDummyApp extends App {
  val log = org.slf4j.LoggerFactory.getLogger(this.getClass)
  
      val conf = new org.apache.spark.SparkConf()

    val (inFile: String, domainClassName: String/*, schemaSelectorsTable:String,
        inputSelectorsTablePath:String, outputSelectorsTablePath:String*/) =
      if (conf.get("spark.inputFile", null) != null) {
        // when run by SparkJobSubmitter
        (
          conf.get("spark."+SparkJobParamKeys.INPUT_FILE),
          conf.get("spark.domainClass")
//          conf.get("spark."+SparkJobParamKeys.DOMAIN_DEVICE_TABLE),
//          conf.get("spark."+SparkJobParamKeys.CURR_SELECTORSTABLE_PATH),
//          conf.get("spark."+SparkJobParamKeys.OUTPUT_PATH)
        )
      } else if (args.length > 0) {
        // when run via cmdline with args
        (args(0), args(1), args(2), args(3), args(4))
      } else { // for testing
        conf.setMaster("local[*]")
        val runTime= System.currentTimeMillis;
      }
         
    val currDir=new java.io.File(".")
    log.info("========= currDir=%s files: %s".format(currDir.getAbsolutePath, currDir.listFiles.toList));

    if(inFile == null || !new File(/*java.net.URI.create*/(inFile)).exists()) {
      throw new RuntimeException("! Cannot find input file: " + inFile )
    }
    
//    println(s"$this: Using $inFile, updating $inputSelectorsTablePath to $outputSelectorsTablePath")
//    log.info(s"Using $inFile, updating $inputSelectorsTablePath to $outputSelectorsTablePath")

//  val conf = new SparkConf().setMaster("local[*]").setAppName("spark-app")
  val sc = new SparkContext(conf)
  val rdd = sc.parallelize(Array(2, 3, 2, 1))
  rdd.saveAsTextFile("build/result")
  sc.stop()
}

