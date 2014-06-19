package com.scalakata.eval


import org.specs2._

class EvalSpecs extends Specification { def is = s2"""
  works $works  
"""

  def works = {
    val c = new Compiler
    c.insight("1+1")
    ok
  }
}