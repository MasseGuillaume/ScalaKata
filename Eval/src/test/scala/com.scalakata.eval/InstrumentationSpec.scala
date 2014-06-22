package com.scalakata.eval

import scala.collection.mutable.{Map => MMap}

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	good $good
"""

	def good = {
		@ScalaKata object SHA {
			val a = "hello"
			a
		}

		SHA.eval$() ==== MMap((241,241) -> "hello")
	}
}