package com.scalakata.eval

import scala.collection.immutable.Queue

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	ident $ident
	desugar $desugar
"""
	def ident = {
		@ScalaKata object A {
			val a = 1
			a
		}
		A.eval$() must beLike {
			case List((_, List(Other("1")))) => ok
			case _ => ko
		}
	}

	def desugar = {
		@ScalaKata object A {
			desugar {
				List(1, 2).map(_+1)

				for {
					i <- 1 to 10
					j <- 1 to 10
				} yield (i, j)
			}
		}
//
//List(((397,493),
//	List(Block(List(
//		((397,493),
//		List("""|```scala
//					  |List(1, 2).map(((x$1) => x$1.+(1)))
//						|```""".stripMargin,
//						```scala
//1.to(10).flatMap(((i) => 1.to(10).map(((j) => scala.Tuple2(i, j)))))
//```)), ((417,422),List(Other(List(2, 3)))), ((441,488),List(Other(Vector((1,1), (1,2), (1,3), (1,4), (1,5), (1,6), (1,7), (1,8), (1,9), (1,10), (2,1), (2,2), (2,3), (2,4), (2,5), (2,6), (2,7), (2,8), (2,9), (2,10), (3,1), (3,2), (3,3), (3,4), (3,5), (3,6), (3,7), (3,8), (3,9), (3,10), (4,1), (4,2), (4,3), (4,4), (4,5), (4,6), (4,7), (4,8), (4,9), (4,10), (5,1), (5,2), (5,3), (5,4), (5,5), (5,6), (5,7), (5,8), (5,9), (5,10), (6,1), (6,2), (6,3), (6,4), (6,5), (6,6), (6,7), (6,8), (6,9), (6,10), (7,1), (7,2), (7,3), (7,4), (7,5), (7,6), (7,7), (7,8), (7,9), (7,10), (8,1), (8,2), (8,3), (8,4), (8,5), (8,6), (8,7), (8,8), (8,9), (8,10), (9,1), (9,2), (9,3), (9,4), (9,5), (9,6), (9,7), (9,8), (9,9), (9,10), (10,1), (10,2), (10,3), (10,4), (10,5), (10,6), (10,7), (10,8), (10,9), (10,10))))))))))
//
//		A.eval$() must beLike {
//			case List((_, List(Other("1")))) => ok
//			case _ => ko
//		}
//	}
}
