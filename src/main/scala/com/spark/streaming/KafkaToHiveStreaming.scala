package com.spark.streaming

import org.apache.spark.sql.{Row, SaveMode, SparkSession}

object KafkaToHiveStreaming {
  case class Record(key: Int, value: String)

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("KafkaToHiveStreaming")
      .enableHiveSupport()
      .getOrCreate()

    import spark.implicits._
    import spark.sql

    // 查询hive表数据，创建表，加载数据
    sql("select * from db_hive.test_01")
    sql(
      "CREATE TABLE IF NOT EXISTS db_hive.src (key INT, value STRING) USING hive"
    )
    sql(
      "LOAD DATA LOCAL INPATH '/Users/chenwei/bigdata/spark-2.4.2/examples/src/main/resources/kv1.txt' INTO TABLE db_hive.src"
    )
    sql("select * from db_hive.src").show()

    // The results of SQL queries are themselves DataFrames and support all normal functions.
    val sqlDF = sql(
      "SELECT key, value FROM db_hive.src WHERE key < 10 ORDER BY key"
    )

    // The items in DataFrames are of type Row, which allows you to access each column by ordinal.
    val stringsDS = sqlDF.map {
      case Row(key: Int, value: String) => s"key : $key ,value: $value "
    }

    stringsDS.show()

    // You can also use DataFrames to create temporary views within a SparkSession.
    val recordsDF =
      spark.createDataFrame((1 to 10000).map(i => Record(i, s"value_$i")))

    recordsDF.createOrReplaceTempView("records")

    // Queries can then join DataFrame data with data stored in Hive.
    sql("SELECT * FROM records r JOIN db_hive.src s ON r.key = s.key").show()

    // Create a Hive managed Parquet table, with HQL syntax instead of the Spark SQL native syntax
    // `USING hive`
    sql(
      "CREATE TABLE  IF NOT EXISTS db_hive.hive_records(key int, value string) STORED AS PARQUET"
    )

    // Save DataFrame to the Hive managed table
    val df = spark.table("db_hive.src")
    df.write.mode(SaveMode.Overwrite).saveAsTable("db_hive.hive_records")

    // After insertion, the Hive managed table has data now
    sql("SELECT * FROM db_hive.hive_records").show()

    // Prepare a Parquet data directory
    val dataDir = "/tmp/parquet_data"
    //    (1 to 10000).toDF("key").write.orc(dataDir)
    spark.range(10).toDF("key").write.orc(dataDir)
    // Create a Hive external Parquet table
    sql(
      s"CREATE EXTERNAL TABLE IF NOT EXISTS db_hive.hive_ints(key int) STORED AS ORC LOCATION '$dataDir'"
    )
    // The Hive external table should already have data
    sql("SELECT * FROM db_hive.hive_ints").show()

    // Turn on flag for Hive Dynamic Partitioning
    spark.sqlContext.setConf("hive.exec.dynamic.partition", "true")
    spark.sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")

    // Create a Hive partitioned table using DataFrame API
    df.write
      .partitionBy("key")
      .format("hive")
      .saveAsTable("db_hive.hive_part_tbl")

    // Partitioned column `key` will be moved to the end of the schema
    sql("select * from db_hive.hive_part_tbl").show()

  }
}
