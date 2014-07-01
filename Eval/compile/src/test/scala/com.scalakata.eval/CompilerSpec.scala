package com.scalakata.eval

import org.specs2._

class CommpilerSpecs extends Specification { def is = s2"""
  works $works
  range $range
"""
  val c = new Compiler

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