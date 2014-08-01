package com.scalakata.eval

import scala.collection.mutable.{Map ⇒ MMap}

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	gracefully handle sideEffects $sideEffects
	cool $cool
"""

	private def sortByPosition(m: MMap[(Int, Int), (String, RenderType)]) =
		m.map{ case (k, v) => (k, (k, v) )}.values.to[List].sortBy(_._1).map(_._2)

	def sideEffects = {
		@ScalaKata
		object A {
			var a = 1
			val b = {a = a + 1; a}
			b
			b
		}

		sortByPosition(A.eval$()) ==== List(
			("1", RT_Other),
			("2", RT_Other),
			("2", RT_Other),
			("2", RT_Other)
		)
	}

	def cool = {
		@ScalaKata
		object A {
			val (a, b) = (1, 2)
			a
			b
			"string"
			<b>xml</b>
			for(i <- 1 to 3) yield i
			List(1, 2, 3)
			md"markdown"
			markdown"## test"
			tex"tex"
			latex"latex"
			html"html"
		}

		sortByPosition(A.eval$()) ==== List(
			("1", RT_Other),
			("2", RT_Other),
			("1", RT_Other),
			("2", RT_Other),
			("string", RT_String),
			("<b>xml</b>", RT_Html),
			("Vector(1, 2, 3)", RT_Other),
			("List(1, 2, 3)", RT_Other),
			("markdown", RT_Markdown),
			("## test", RT_Markdown),
			("tex", RT_Latex),
			("latex", RT_Latex),
			("html", RT_Html)
		)
	}
}
