package com.scalakata.eval

import org.specs2._

class CommpilerSpecs extends Specification { def is = s2"""
  works $works
"""
  val c = new Compiler

  def assert(code: String, expected: String) = {
    val i = c.insight(code)
    println(i.insight)
  	val EvalResponse(List(Instrumentation(result,_,_)), _, _, _) = i
  	result ==== expected
  }

  def works = {
    assert(
    	"""|val a = 1+1
    	   |a
    	   |""".stripMargin, 

		"2"
	 )
  }
}