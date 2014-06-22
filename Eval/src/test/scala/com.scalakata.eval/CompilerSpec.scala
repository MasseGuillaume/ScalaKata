package com.scalakata.eval


import org.specs2._

class CommpilerSpecs extends Specification { def is = s2"""
  works $works
  value classes $valueClass
"""
  val c = new Compiler

  def assert(code: String, expected: String) = {
  	val EvalResponse(List(Instrumentation(result,_)), _, _, _) = c.insight(code)
  	result ==== expected
  }

  def works = {
    assert("1+1", "2")
  }

  def valueClass = {
  	val code = 
  		"""|class Meter(val value: Int) extends AnyVal {
  		   |	def +(m: Meter): Meter = new Meter(value + m.value)
		   |}
  		   |val x = new Meter(3)
		   |val y = new Meter(4)
		   |x + y
  		   |""".stripMargin
  	assert(code, "7")
  }
}