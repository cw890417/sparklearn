package com.spark.rdd
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 两种主要获取数据源的方式：
  * 1）自己定义数据源
  * 2）外部获取数据源
  */
object rddBase {
  def main(args: Array[String]): Unit = {
    // 1. 设置conf：程序名称，master，并初始化SparkContext
    val conf = new SparkConf().setAppName("spark rdd").setMaster("local")
    val sc = new SparkContext(conf)

    //自己初始化数据
//    println(paralleDataSet(sc))

    //通过外部获取数据
//    externalDateSet(sc).foreach(println)
    externalDateSet(sc)
//    println(externalDateSet(sc))

  }

  def paralleDataSet(sc: SparkContext) = {
    //两种创建数据集的方式：1）自己生成数据
    val data = Array(1, 2, 3, 4)
    val distData = sc.parallelize(data)
    distData.reduce((a, b) => a + b)
  }

  def externalDateSet(sc: SparkContext) = {
    // 2）外部数据集
    val data = sc.textFile("/Users/chenwei/bigdata/spark2.4/data/rdd/")
    // 将单词转换成小写，在进行统计，
    // 使用repartition使最终的文件只有一个，但是这样是否会增加节点的压力？
    data
      .flatMap(_.split(" "))
      .map(a => (a.toLowerCase, 1))
      .reduceByKey(_ + _)
      .repartition(1)
      .saveAsTextFile("/Users/chenwei/bigdata/spark2.4/data/output/")

//    data.map(a => a.length).reduce(_ + _)
  }

}
