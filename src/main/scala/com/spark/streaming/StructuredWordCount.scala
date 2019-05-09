package com.spark.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode

object StructuredWordCount {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName(" structured streaming word count")
//      .master("local[2]")
      .getOrCreate()

    import spark.implicits._

    // 创建数据链接，并读取数据
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", "9999")
      .load()

    // 获取数据，将数据重dataframe 转成 dataset
    val words = lines.as[String].flatMap(_.split(" "))

    // 调用count()时，又将dataset转成了dataframe。。
    val wordCounts = words.groupBy("value").count()

    // 创建查询datafream
    val query = wordCounts.writeStream
      .outputMode(OutputMode.Update())
      .format("console")
      .start()

    query.awaitTermination()

  }
}
