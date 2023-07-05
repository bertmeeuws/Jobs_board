package com.bmmedia.playground

trait Printable[A] {
  def format(value: A): String
}

object PrintableInstances {
  implicit val stringPrintable: Printable[String] = new Printable[String] {
    def format(value: String): String = value
  }
  implicit val intPrintable: Printable[Int] = new Printable[Int] {
    def format(value: Int): String = value.toString
  }
}

import PrintableInstances._

object Printer {
  def print[A](value: A)(implicit p: Printable[A]): Unit = {
    println(p.format(value))
  }
}

implicit class Log[String](value: String) {
  def debug()(implicit k: (a: String) => Unit) = {
    println(value)
    k(value)
  }
}

object playground {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")

    val test = Printer.print("Hello").debug()
    Printer.print(123)
  }
}
