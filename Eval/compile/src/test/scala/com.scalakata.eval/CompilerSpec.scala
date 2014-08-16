package com.scalakata.eval

import sbt.BuildInfo

import java.io.File

import org.specs2._

class CommpilerSpecs extends Specification { def is = s2"""
  works $works
"""
  /*runtimeErrors $runtimeErrors
  compile classpath $compileClasspath
  typeAt $typeAt
  autocomplete symbols $autocompleteSymbols
  autocomplete types $autocompleteTypes*/

  def wrap(code: String) =
    s"""|import com.scalakata.eval._
        |@ScalaKata object Playground{
        | $code
        |}""".stripMargin

  val artifacts =
    (BuildInfo.fullClasspath ++ BuildInfo.runtime_exportedProducts).
      map(_.getAbsoluteFile).
      mkString(File.pathSeparator)

  val scalacOptions = sbt.BuildInfo.scalacOptions.to[Seq]
  def compiler = new Compiler(artifacts, scalacOptions)

  def works = {
    val c = compiler
    val code =
    	"""|val a = List(1,2)
         |val b = 2""".stripMargin

    val result = c.insight(
      s"""|package intro
          |class TEEEEEEEEEEEEEEST()
          |${wrap(code)}""".stripMargin
    )

    result ==== EvalResponse.empty.copy(insight =
      List(Instrumentation("List(1, 2)", RT_Other, 62, 75), Instrumentation("2", RT_Other, 80, 85))
    )
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
      insight = List(Instrumentation("123", RT_Other, 83, 94))
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
      CompletionResponse("map","[B](f: A => B): scala.collection.TraversableOnce[B]")
    )
  }
}
