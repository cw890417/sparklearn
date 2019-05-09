package com.spark.sql
import org.apache.spark.sql.SparkSession

object SQLDataSourceExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[2]")
      .appName("Data source")
      .getOrCreate()

    runBaseDataSource(spark)

    spark.stop()
  }

  private def runBaseDataSource(spark: SparkSession) = {
    val userDF = spark.read.load(
      "/Users/chenwei/bigdata/spark2.4/examples/src/main/resources/users.parquet"
    )
    userDF.cache()
    userDF.printSchema()
    userDF
      .select("name", "favorite_color")
      .write
      .format("json")
      .mode("overwrite")
      .save("/Users/chenwei/bigdata/spark2.4/data/sql/users_name.json")

    val peopleDF = spark.read
      .format("json")
      .load(
        "/Users/chenwei/bigdata/spark2.4/examples/src/main/resources/people.json"
      )
    peopleDF.cache()
    peopleDF.printSchema()
    peopleDF.write
      .format("parquet")
      .mode("overwrite")
      .save("/Users/chenwei/bigdata/spark2.4/data/sql/people.parquet")

    val peopleDFCsv = spark.read
      .format("csv")
      .option("sep", ";")
      .option("inferSchema", "true")
      .option("header", "true")
      .load(
        "/Users/chenwei/bigdata/spark2.4/examples/src/main/resources/people.csv"
      )
    peopleDFCsv.cache()
    peopleDFCsv.show()

    peopleDFCsv.write
      .format("orc")
      .mode("append")
      .save("/Users/chenwei/bigdata/spark2.4/data/sql/people_orc.orc")

    val sqlDF = spark
      .sql(
        "select * from orc.`/Users/chenwei/bigdata/spark2.4/data/sql/people_orc.orc`"
      )
      .show()

  }
}
