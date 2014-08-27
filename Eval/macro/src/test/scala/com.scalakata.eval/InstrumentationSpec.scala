package com.scalakata.eval

import scala.collection.mutable.{Set ⇒ MSet}

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	test $test
"""

	def test = {
		@ScalaKata
		object A {
			identity(1)
			latex"latex"
			html"html"
			markdown"markdown";
			{
				html"html2"
				markdown"markdown2"
			}
			1+1
		}

		val before = Ordering[Range].lt _
		sortByRange(A.eval$()) must beLike {
			case List(
				Other("1"),
				Latex("latex"),
				Html("html"),
				Markdown("markdown"),
				Block(List(
					(pb1, Html("html2")),
					(pb2, Markdown("markdown2"))
				)),
				Other("2")
			) if(before(pb1, pb2)) => ok
			case _ => ko
		}
	}


	private def sortByRange(vs: MSet[(Range, Render)]) = {
		vs.to[List].sortBy(_._1).map(_._2)
	}

	/*gracefully handle sideEffects $sideEffects
	cool $cool
	desugar $desugarTest
	dont instrument val, def & type = $preserve
	block $block*/

	/*private def sortByPosition(m: MMap[(Int, Int), (String, RenderType)]) =
		m.map{ case (k, v) => (k, (k, v) )}.values.to[List].sortBy(_._1).map(_._2)*/

	/*def desugarTest = {
		@ScalaKata
		object A {
			desugar {
				for {
        	i <- 1 to 10
            j <- 1 to 10
        } yield (i * j)
			}
		}

		A.eval$().values.to[List].map(_._1) must contain ((v: String) => v must contain("flatMap"))
	}*/

	/*def preserve = {
		@ScalaKata
		object A {
			type T = Int
			val a = 1
			def b = 2
		}
		val res: A.T = 1
		A.a ==== 1 &&
		A.b ==== 2
	}*/

	/*def sideEffects = {
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
	}*/

	/*def block = {
		@ScalaKata
		object A {
			identity(0)

			{
				identity(1)
				identity(2)
				identity(3)
			}

			identity(4)

			{
				identity(5)
			}

			identity(6)
		}

		sortByPosition(A.eval$()) ==== List(
			("0", RT_Other),
			("3", RT_Block),
			("1", RT_Other),
			("2", RT_Other),
			("3", RT_Other),
			("4", RT_Other),
			("5", RT_Block),
			("5", RT_Other),
			("6", RT_Other)
		)
	}*/
}
