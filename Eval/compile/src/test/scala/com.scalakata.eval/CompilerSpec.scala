package com.scalakata.eval

import sbt.BuildInfo

import java.io.File

import org.specs2._

class CommpilerSpecs extends Specification { def is = s2"""
  linkage $linkage
  doubledef $doubledef
"""

  def doubledef = {
    val c = compiler
    val code = """|trait K[T] { def f = A }
                  |case object A extends K[Int]""".stripMargin

    val result = c.insight(wrap(code))
    result.runtimeError must be empty
  }

  /*

  nopackage $nopackage
  compilation infos $infos
  runtimeErrors $runtimeErrors
  works $works
  support packages $packages
  compile classpath $compileClasspath
  typeAt $typeAt
  autocomplete symbols $autocompleteSymbols
  autocomplete types $autocompleteTypes*/

  def wrap(code: String) =
    s"""|import com.scalakata.eval._
        |@ScalaKata object Playground {
        | $code
        |}""".stripMargin

  val artifacts =
    (BuildInfo.fullClasspath ++ BuildInfo.runtime_exportedProducts).
      map(_.getAbsoluteFile).
      mkString(File.pathSeparator)

  val scalacOptions = sbt.BuildInfo.scalacOptions.to[Seq]
  def compiler = new Compiler(artifacts, scalacOptions, security = false)

  def linkage = {
    val c = compiler
    val inner = """|(new BC).f
                   |(new CB).f""".stripMargin

    val code = s"""|trait A { def f: String }
                   |trait B extends A { override def f = "B" }
                   |trait C extends A { override def f = "C" }
                   |class BC extends A with B with C
                   |class CB extends A with C with B
                   |${wrap(inner)}""".stripMargin
    val result = c.insight(code)
    result.runtimeError must be empty
  }

  def nopackage = {
    val c = compiler
    val result = c.insight(wrap("1+1"))
    result.insight must not be empty
  }

  def infos = {
    val c = compiler
    val result = c.insight(
      """|object Extras {
         |  type V = Toto
         |  class Meter(val v: Int) extends Anyval {
         |    def +(o: Meter) = new Meter(v + o.v)
         |  }
         |}""".stripMargin)
    result.infos must not be empty
  }

  def works = {
    val c = compiler
    val result = c.insight(wrap("1+1"))
    result.insight must not be empty
  }

  def packages = {
    val c = compiler
    val code =
    	"""|val a = List(1,2)
         |val b = 2""".stripMargin

    val result = c.insight(
      s"""|package intro
          |class TEEEEEEEEEEEEEEST()
          |${wrap(code)}""".stripMargin
    )

    result.insight must not be empty
    /*result ==== EvalResponse.empty.copy(insight =
      List(Instrumentation("List(1, 2)", RT_Other, 62, 75), Instrumentation("2", RT_Other, 80, 85))
    )*/
  }


  def runtimeErrors = {
    val c = compiler
    val code =
     """|0
        |1/0""".stripMargin
    c.insight(wrap(code)) ==== EvalResponse.empty.copy(
      runtimeError = Some(RuntimeError("java.lang.ArithmeticException: / by zero", 2))
    )
  }

  def compileClasspath = {
    val c = compiler
    c.insight(wrap("com.example.test.Testing.onetwothree")) ==== EvalResponse.empty.copy(
      insight = List(((83, 94), List(Other("123"))))
    )
  }

  def typeAt = {
    val c = compiler
    c.typeAt(wrap("List(1)"), 65, 65) ====
      Some(TypeAtResponse("List[Int]"))
  }

  def autocompleteSymbols = {
    val c = compiler
    c.autocomplete(" ", 0) must contain(
      CompletionResponse(
        name = "assert",
        signature = "(assertion: Boolean): Unit"
      )
    )
  }

  def autocompleteTypes = {
    val c = compiler
    c.autocomplete(wrap("List(1)."), 66) must contain(
      CompletionResponse("map","[B](f: A ⇒ B): scala.collection.TraversableOnce[B]")
    )
  }
}
