package com.spark.scalaprog

/**
  * 匹配模式详解
  */
object BaseScalaProg {
  def main(args: Array[String]): Unit = {
//    print(forYield)
//    matchVariable
//    matchSeq
//    matchTuple
//    matchGuard
//    matchDeepTuple
    matchDeep2
  }

  /**
    * 通过for过滤数据，在通过yield组成集合，数据赋给filteredBreeds
    */
  private def forYield: List[String] = {
    val dogBreeds = List(
      "Doberman",
      "Yorkshire Terrier",
      "Dachshund",
      "Scottish Terrier",
      "Great Dane",
      "Portuguese Water Dog"
    )

    for {
      attr <- dogBreeds
      if attr.contains("Terrier") && !attr.startsWith("Yorkshire")
    } yield attr.toUpperCase

  }

  /**
    * match 匹配
    * 值匹配
    * 类型匹配
    * 覆盖所有值的可能
    * _站位符
    * 当输出的内容相同是可以将匹配的条件通过"或"|写在一起
    */
  private def matchVariable: Unit = {
    for (x <- Seq(1, 2, 2.7, "one", "two", 'four, true)) {
      val str = x match {
        case 1                  => "int 1"
        case _: Int | _: Double => "a number " + x
        case "one"              => "string one"
        case s: String          => "other string " + s
        case _: Boolean         => "boolean is " + x
        case _                  => "unexpected value: " + x
      }
      println(str)
    }
  }

  /**
    *
    */
  private def matchSeq: Unit = {
    val nonEmptySeq = Seq(1, 2, 3, 4, 5)
    val emptySeq = Seq.empty[Int]
    val nonEmptyList = List(1, 2, 3, 4, 5)
    val emptyList = Nil
    val nonEmptyVector = Vector(1, 2, 3, 4, 5)
    val emptyVector = Vector.empty[Int]
    val nonEmptyMap = Map("one" -> 1, "two" -> 2, "three" -> 3)
    val emptyMap = Map.empty[String, Int]

    for (seq <- Seq(
           nonEmptySeq,
           emptySeq,
           nonEmptyList,
           emptyList,
           nonEmptyVector,
           emptyVector,
           nonEmptyMap.toSeq,
           emptyMap.toSeq
         )) {
      println(seqToString(seq))
    }

  }

  /**
    * 元组匹配
    */
  private def matchTuple: Unit = {
    val langs = Seq(
      ("Scala", "Martin", "Odersky"),
      ("Clojure", "Rich", "Hickey"),
      ("Lisp", "John", "McCarthy")
    )

    for (tuple <- langs) {
      tuple match {
        case ("Scala", _, _) => println("found scala")
        case (lang, first, last) =>
          println(s"Found other language: $lang ($first, $last)")
      }

    }
  }

  /**
    * 增加守卫条件
    */
  private def matchGuard: Unit = {
    for (i <- Seq(1, 2, 3, 4)) {
      i match {
        case _ if i % 2 == 0 => println(s"even: $i")
        case _               => println(s"odd:  $i")
      }
    }
  }

  /**
    * zip元组进行匹配
    */
  private def matchDeepTuple: Unit = {
    val itemsCosts = Seq(("Pencil", 0.52), ("Paper", 1.35), ("Notebook", 2.43))
    val itemsCostsIndices = itemsCosts.zipWithIndex
    for (itemCostIndex <- itemsCostsIndices) {
      itemCostIndex match {
        case ((item, cost), index) => println(s"$index: $item costs $cost each")
      }
    }
  }

  case class Address(street: String, city: String, country: String)
  case class Person(name: String, age: Int, address: Address)

  /**
    * 变量绑定匹配
    */
  private def matchDeep2: Unit = {
    val alice = Person("Alice", 25, Address("1 Scala Lane", "Chicago", "USA"))
    val bob = Person("Bob", 29, Address("2 Java Ave.", "Miami", "USA"))
    val charlie =
      Person("Charlie", 32, Address("3 Python Ct.", "Boston", "USA"))

    for (person <- Seq(alice, bob, charlie)) {
      person match {
        case p @ Person("Alice", 25, address) => println(s"Hi Alice! $p")
        case p @ Person("Bob", 29, a @ Address(street, city, country)) =>
          println(s"Hi ${p.name}! age ${p.age}, in ${a.city}")
        case p @ Person(name, age, _) =>
          println(s"Who are you, $age year-old person named $name? $p")
      }
    }
  }

  /**
    * 递归获取值
    *
    * @param seq
    * @tparam T
    * @return
    */
  private def seqToString[T](seq: Seq[T]): String = {
    seq match {
      case head +: tail => s"($head +: + ${seqToString(tail)})"
      case Nil          => "(Nil)"
    }
  }

}
