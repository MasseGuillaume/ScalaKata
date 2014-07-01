package com.scalakata.eval

import scala.collection.mutable.{Map ⇒ MMap}

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	range is relative to macro $relative
"""

	def relative = {
		@ScalaKata
		object A{object B{0
			1
		}}

		Instrumented.eval$() ==== MMap(
			(0,1) -> 0,
			(5,6) -> 1
		)
	}
}