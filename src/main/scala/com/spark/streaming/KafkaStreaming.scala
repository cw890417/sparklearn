package com.spark.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode

object KafkaStreaming {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("KafkaStreaming")
//      .master("local[2]")
      .getOrCreate()

    import spark.implicits._

    val lines = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "topic1")
//      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(value AS STRING)")
      .as[String]

    val wordCounts = lines.flatMap(_.split(" ")).groupBy("value").count()

    val query =
      wordCounts.writeStream
        .format("console")
        .outputMode(OutputMode.Update())
        .start()

    query.awaitTermination()

  }
}
