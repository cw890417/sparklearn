package com.spark.scalaprog

/**
  * 继承和特质的执行顺序
  * 首先执行extends 的类，然后从左到又执行特质
  */
object traitExample {
  trait Logger {
    println("Logger")
  }

  trait FileLogger extends Logger {
    println("FileLogger")
  }

  trait Closable {
    println("Closable")
  }

  class Person {
    println("Constructing Person ...")
  }

  class Student extends Person with FileLogger with Closable {
    println("Constructing Student ...")
  }

  def main(args: Array[String]): Unit = {
    new Student
  }
}
