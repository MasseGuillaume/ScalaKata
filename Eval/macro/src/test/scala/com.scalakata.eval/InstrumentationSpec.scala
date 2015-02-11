package com.scalakata.eval

import scala.collection.immutable.Queue

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	imports $imports
"""
  def imports = {
		@ScalaKata object P {
			import scala.collection.mutable.Stack
			case class VV(a: Stack[Int])
			VV(Stack(1))
		}
		P.eval$() must not be empty
	}
}