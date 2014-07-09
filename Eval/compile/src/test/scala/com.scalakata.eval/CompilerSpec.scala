package com.scalakata.eval

import sbt.BuildInfo

import java.io.File

import org.specs2._

class CommpilerSpecs extends Specification { def is = s2"""
  works $works
  range $range
"""

  val artifacts =
    (BuildInfo.dependencyClasspath ++ BuildInfo.runtime_exportedProducts).
      map(_.getAbsoluteFile).
      mkString(File.pathSeparator)

  val scalacOptions = sbt.BuildInfo.scalacOptions.to[Seq]
  
  val c = new Compiler(artifacts, scalacOptions)

  def works = {
    // val code = 
    // 	"""|val a = List(1,2)
    //      |val b = 2""".stripMargin

    // val result = c.insight(code)

    // result ==== EvalResponse.empty.copy(insight = 
    //   List(Instrumentation("List(1,2)",13,13), Instrumentation("2",10,10))
    // )
    ok
  }

  def range = {
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
}