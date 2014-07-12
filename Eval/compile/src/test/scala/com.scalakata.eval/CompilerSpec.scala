package com.scalakata.eval

import sbt.BuildInfo

import java.io.File

import org.specs2._

class CommpilerSpecs extends Specification { def is = s2"""
  works $works
  range $range
  runtimeErrors $runtimeErrors
  compile classpath $compileClasspath
  typeAt $typeAt
"""

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

    val result = c.insight(code)

    result ==== EvalResponse.empty.copy(insight = 
      List(Instrumentation("List(1, 2)",4,17), Instrumentation("2",22,27))
    )
  }

  def range = {
    val c = compiler
    val code = 
     """|0
        |1""".stripMargin

    c.insight(code) ==== EvalResponse.empty.copy(insight =
      List(
        Instrumentation("1", 2, 3),
        Instrumentation("0", 0, 1)
      )
    )
  }

  def runtimeErrors = {
    val c = compiler
    val code = 
     """|0
        |1/0""".stripMargin
    c.insight(code) ==== EvalResponse.empty.copy(
      runtimeError = Some(RuntimeError("java.lang.ArithmeticException: / by zero", 2))
    )
  }

  def compileClasspath = {
    val c = compiler
    c.insight("com.example.test.Testing.onetwothree") ==== EvalResponse.empty.copy(
      insight = List(Instrumentation("123", 25, 36))
    )
  }
  
  // def works = ok
  // def range = ok
  // def runtimeErrors = ok
  // def compileClasspath = ok

  def typeAt = {
    val c = compiler
    c.typeAt("""val a = List(1,2,3,4).groupBy(identity)""", 3, 4) ==== 
      Some(TypeAtResponse("scala.collection.immutable.Map[Int,List[Int]]"))
  }
}