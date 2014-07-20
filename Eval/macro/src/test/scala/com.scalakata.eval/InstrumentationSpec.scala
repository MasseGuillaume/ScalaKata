package com.scalakata.eval

import scala.collection.mutable.{Map ⇒ MMap}

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	range is relative to macro $relative
	extract value class $valueClasses
	avoid side effects $noSideEffects
"""

	def relative = {
		@ScalaKata object Instrumented{object A{0
			1
		}}

		Instrumented.eval$() ==== MMap(
			(0,1) -> 0,
			(5,6) -> 1
		)
	}
	
	def valueClasses = {
		@ScalaKata object Instrumented{object A{
			class Meter(val value: Double) extends AnyVal {
			  def +(m: Meter): Meter = new Meter(value + m.value)
			}
			val x = new Meter(3.4)
			val y = new Meter(4.3)
			val z = x + y
		}}
		Instrumented.eval$() ==== MMap()
	}

	def noSideEffects = {
		pending
	}
}