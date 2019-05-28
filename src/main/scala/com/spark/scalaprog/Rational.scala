package com.spark.scalaprog

/**
  * 1.定义多个构造器
  * 2.重写toString方法，使用到了math
  * 3.定义操作符
  * 4.使用到了隐式转换
  *
  * @param n 分子
  * @param d 分母
  */
class Rational(n: Int, d: Int) {
  // 分母不能为0
  require(d != 0)

  private val g = gcd(n.abs, d.abs)
  val numer: Int = n / g
  val denom: Int = d / g

  // 辅助构造方法
  def this(n: Int) = this(n, 1)

  override def toString: String = {
    (numer, denom) match {
      case (_, _) if (numer % denom) == 0 =>
        (numer / denom).toString
      case (_, _) if numer == 0 => 0.toString
      case _                    => numer + "/" + denom
    }
  }

  // 两个Rational相加
  def +(that: Rational): Rational = {
    new Rational(numer * that.denom + that.numer * denom, denom * that.denom)
  }

  // + 方法重载
  def +(i: Int): Rational = {
    new Rational(numer + i * denom, denom)
  }

  // 两个Rational相减
  def -(that: Rational): Rational = {
    new Rational(numer * that.denom - that.numer * denom, denom * that.denom)
  }

  // - 方法重载
  def -(i: Int): Rational = {
    new Rational(numer - i * denom, denom)
  }

  // 两个Rational相乘
  def *(that: Rational): Rational = {
    new Rational(numer * that.numer, denom * that.denom)
  }

  def *(i: Int): Rational = {
    new Rational(numer * i, denom)
  }

  def /(that: Rational): Rational = {
    new Rational(numer * that.denom, denom * that.numer)
  }

  def /(i: Int): Rational = {
    new Rational(numer, denom * i)
  }

  private def gcd(a: Int, b: Int): Int = {
    if (b == 0) a else gcd(b, a % b)
  }

}

object Rational {

  /**
    * 定义隐式
    *
    * @param x
    * @return
    */
  implicit def intToRational(x: Int) = new Rational(x)

  def main(args: Array[String]): Unit = {
    val a = new Rational(1, 2)
    val b = new Rational(1, 2)
    println(a + b)
    // 优先级，*比+高
    println(a + a * b)
    println(a * 2)
    println(a - b)
    println(a / b)

    // 隐式转换
    println(5 * a)
  }
}
