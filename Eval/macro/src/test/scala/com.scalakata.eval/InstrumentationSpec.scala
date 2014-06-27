package com.scalakata.eval

import scala.collection.mutable.{Map => MMap}

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	good $good
	all $all
"""

	def good = {
		@ScalaKata object SHA {
			val allo = "hello"
			allo
			val placeholder = "toto"
			placeholder
		}

		// fail because of range pos
		SHA.eval$() ==== MMap(
			(244, 248) -> "hello",
			(280, 291) -> "toto"
		)
	}


	def all = {
		@ScalaKata object SHA {
			val (a, b) = (1, 2)
			var (c, d) = (3, 4)
			List.empty[Int]
			List.fill(2)("t")
		}
		println(SHA.eval$())
		ok
	}
}