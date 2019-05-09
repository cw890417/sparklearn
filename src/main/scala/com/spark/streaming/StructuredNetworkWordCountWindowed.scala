package com.spark.streaming


import java.sql.Timestamp

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object StructuredNetworkWordCountWindowed {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("spark://localhost:7077")
      .appName("StructuredNetworkWordCountWindowed")
      .getOrCreate()

    val host = "localhost"
    val port = "9999"
    val windowSize = 10
    val slideSize = 5

    import spark.implicits._

    val lines = spark.readStream
      .format("socket")
      .option("host", host)
      .option("port", port)
      .option("includeTimestamp", true)
      .load()

    val words = lines
      .as[(String, Timestamp)]
      .flatMap(line => line._1.split(" ").map(word => (word, line._2)))
      .toDF("word", "timestamp")

    val windowedCounts = words
      .groupBy(
        window($"timestamp", s"$windowSize seconds", s"$slideSize seconds"),
        $"word"
      )
      .count()
      .orderBy("window")

    val query = windowedCounts.writeStream
      .outputMode("complete")
      .format("console")
      .option("truncate", "false")
      .start()

    query.awaitTermination()

  }
}
