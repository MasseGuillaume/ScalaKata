package com.scalakata.eval

import scala.collection.mutable.{Map ⇒ MMap}

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	no side effects $noSideEffects
"""

	def noSideEffects = {
		@ScalaKata
		object A{
			var a = 1
			val b = {a = a + 1; a}
			b
			b
		}

		A.eval$().map{ case (k, v) => (k, (k, v) )}.values.to[List].sortBy(_._1).map(_._2) ==== List(
			("1", Other),
			("2", Other),
			("2", Other),
			("2", Other)
		)
	}
}
