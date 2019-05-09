package com.spark.sql
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

object SparkSqlTest {
  case class Employees(name: String, salary: Long)
  case class Person(name: String, age: Long)
  val filePath =
    "/Users/chenwei/bigdata/spark2.4/examples/src/main/resources/employees.json"

  def main(args: Array[String]): Unit = {
    val spark =
      SparkSession
        .builder()
        .master("local[1]")
        .appName("spark sql test")
        .getOrCreate()

//    runBasicDataFrameExample(spark)
//    runDatasetCreationExample(spark)
//    runInferSchemaExample(spark)
    runProgrammaticSchemaExample(spark)

    spark.stop()
  }

  def runBasicDataFrameExample(spark: SparkSession) = {
    val df = spark.read.json(filePath)
    df.persist()
    df.show()

    df.printSchema()
    df.select("name", "salary").show()
    // 使用$ 需要做隐式转换
    import spark.implicits._
    df.select($"name", $"salary" + 1).show()
    df.filter($"salary" > 3500).show()
    df.groupBy("name", "salary").count().show()

    // 创建临时视图，在当前session有效
    df.createOrReplaceTempView("employees")
    spark.sql("select * from employees where name = 'Andy'").show()

    // 创建全局临时视图，在整个生命周期中有效
    df.createGlobalTempView("employees")
    spark.sql("select * from global_temp.employees where name = 'Andy'").show()

  }

  def runDatasetCreationExample(spark: SparkSession) = {
    // 使用$ 需要做隐式转换
    import spark.implicits._

    val caseClassDS = Seq(Employees("chenwei", 20000)).toDS()
    caseClassDS.show()

    val employeesDS = spark.read.json(filePath).as[Employees]
    employeesDS.show()

  }

  def runInferSchemaExample(spark: SparkSession): Unit = {
    // $example on:schema_inferring$
    // For implicit conversions from RDDs to DataFrames
    import spark.implicits._

    // Create an RDD of Person objects from a text file, convert it to a Dataframe
    val peopleDF = spark.sparkContext
      .textFile(
        "/Users/chenwei/bigdata/spark2.4/examples/src/main/resources/people.txt"
      )
      .map(_.split(","))
      .map(attributes => Person(attributes(0), attributes(1).trim.toInt))
      .toDF()
    // Register the DataFrame as a temporary view
    peopleDF.createOrReplaceTempView("people")

    // SQL statements can be run by using the sql methods provided by Spark
    val teenagersDF =
      spark.sql("SELECT name, age FROM people WHERE age BETWEEN 13 AND 19")

    teenagersDF.show()

    // The columns of a row in the result can be accessed by field index
    teenagersDF
      .map(
        teenager => "Name: " + teenager(0) + ",Age: " + teenager.getAs[Long](1)
      )
      .show()
    // +------------+
    // |       value|
    // +------------+
    // |Name: Justin,Age:
    // +------------+

    // or by field name
    teenagersDF
      .map(teenager => "Name: " + teenager.getAs[String]("name"))
      .show()
    // +------------+
    // |       value|
    // +------------+
    // |Name: Justin|
    // +------------+

    // No pre-defined encoders for Dataset[Map[K,V]], define explicitly
    implicit val mapEncoder =
      org.apache.spark.sql.Encoders.kryo[Map[String, Any]]
    // Primitive types and case classes can be also defined as
    // implicit val stringIntMapEncoder: Encoder[Map[String, Any]] = ExpressionEncoder()

    // row.getValuesMap[T] retrieves multiple columns at once into a Map[String, T]
    teenagersDF
      .map(teenager => teenager.getValuesMap[Any](List("name", "age")))
      .collect()
      .foreach(print)
    // Array(Map("name" -> "Justin", "age" -> 19))
    // $example off:schema_inferring$
  }

  def runProgrammaticSchemaExample(spark: SparkSession): Unit = {
    import spark.implicits._
    // $example on:programmatic_schema$
    // Create an RDD
    val peopleRDD =
      spark.sparkContext.textFile(
        "/Users/chenwei/bigdata/spark2.4/examples/src/main/resources/people.txt"
      )

    // The schema is encoded in a string
    val schemaString = "name age"

    // Generate the schema based on the string of schema
    val fields = schemaString
      .split(" ")
      .map(fieldName => StructField(fieldName, StringType, nullable = true))
    val schema = StructType(fields)

    // Convert records of the RDD (people) to Rows
    val rowRDD = peopleRDD
      .map(_.split(","))
      .map(attributes => Row(attributes(0), attributes(1).trim))

    // Apply the schema to the RDD
    val peopleDF = spark.createDataFrame(rowRDD, schema)

    // Creates a temporary view using the DataFrame
    peopleDF.createOrReplaceTempView("people")

    // SQL can be run over a temporary view created using DataFrames
    val results = spark.sql("SELECT name,age FROM people")

    // The results of SQL queries are DataFrames and support all the normal RDD operations
    // The columns of a row in the result can be accessed by field index or by field name
    results
      .map(attributes => "Name: " + attributes(0) + ",Age :" + attributes(1))
      .show()
    // +-------------+
    // |        value|
    // +-------------+
    // |Name: Michael|
    // |   Name: Andy|
    // | Name: Justin|
    // +-------------+
    // $example off:programmatic_schema$
  }
}
