package com.spark.streaming

import org.apache.spark.sql.SparkSession

object KafkaToHiveStreaming {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("spark://localhost:7077")
      .appName("KafkaToHiveStreaming")
      .enableHiveSupport()
      .getOrCreate()

//    import spark.implicits._
    import spark.sql
    sql("select * from db_hive.test_orc").show()

  }
}
