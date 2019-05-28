package com.spark.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode

object HiveOnSpark {
  def main(args: Array[String]): Unit = {
    val path =
      "/Users/chenwei/bigdata/spark-2.4.2/examples/src/main/resources/people.txt"

    val spark = SparkSession
      .builder()
      .master("spark://localhost:7077")
      .appName("HiveOnSpark")
      .enableHiveSupport()
      .getOrCreate()
    //获取hive表的字段信息
    getColumens(spark, "db_hive", "test_orc")
//    readTextToHiveOrc(spark, path)
//    readStreamToHive(spark)
  }

  def getColumens(spark: SparkSession, db_name: String, tb_name: String) = {

    import spark.implicits._
//    val cols = spark.catalog
//      .listColumns(db_name, tb_name)
//      .filter(col => col.dataType == "string")
//      .map(col => col.name)
//      .collect
//
//    for (value <- cols) {
//      println(value)
//    }
    import spark.sql
    sql("select * from db_hive.aaa").show()

  }

  case class People(name: String, age: Int)
  def readTextToHiveOrc(spark: SparkSession, path: String) = {

    import spark.implicits._
    val df = spark.sparkContext
      .textFile(path)
      .map(_.split(","))
      .map(value => People(value(0).trim, value(1).trim.toInt))
      .toDF("name", "age")
    df.createOrReplaceTempView("temp_01")

    import spark.sql
    //创建hive表，orc格式
    sql("select * from temp_01").show()
    sql(
      "CREATE TABLE IF NOT EXISTS db_hive.temp_orc (name STRING, age INT) STORED AS ORC"
    )
    sql("insert into table db_hive.temp_orc select * from temp_01")

    sql("select * from db_hive.temp_orc").show()

  }

  /**
    * 新增加的数据能查询到，但是之前的表的数据查询不到，是什么原因？
    * @param spark
    */
  def readStreamToHive(spark: SparkSession) = {
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", "9999")
      .load()

    import org.apache.spark.sql.streaming.Trigger
    import spark.implicits._
    val df = lines
      .as[String]
      .map(_.split(" "))
      .map(attr => People(attr(0).trim, attr(1).trim.toInt))
      .toDF("name", "age")
      .writeStream
      .outputMode(OutputMode.Append())
      .format("orc")
      .trigger(Trigger.ProcessingTime("30 seconds"))
      .option("checkpointLocation", "/tmp/checkpointLocation")
      .option(
        "path",
        "hdfs://localhost:9000/user/hive/warehouse/db_hive.db/temp_orc"
      )
      .start()

    df.awaitTermination()

  }

}
