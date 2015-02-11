package com.scalakata.eval

import scala.collection.immutable.Queue

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	alias $alias
"""
  def alias = {
		@ScalaKata object P {
			type B = String
			case class A(b: B)
			A("b")
		}
		P.eval$() must not be empty
	}
}