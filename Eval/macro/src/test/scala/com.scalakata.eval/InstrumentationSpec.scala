package com.scalakata.eval

import scala.collection.immutable.Queue

import org.specs2._

class InstrumentationSpecs extends Specification { def is = s2"""
	desugar $desugar
"""
  //ident $ident
	//def ident = {
	//	@ScalaKata object A {
	//		val a = 1
	//		a
	//	}
	//	A.eval$() must beLike {
	//		case List((_, List(Other("1")))) ⇒ ok
	//		case _ ⇒ ko
	//	}
	//}

	//dynamic $dynamic
	//def dynamic = {
	//	import scala.language.dynamics
	//	@ScalaKata object A {
	//		object Proxy extends Dynamic {
	//		  def selectDynamic(name: String) = s"selectDynamic $name"
	//		  def updateDynamic(name: String)(value: Any) = s"$name $value"
	//		  def applyDynamicNamed(name: String)(args: (String, Any)*) = {
	//		    val args2 = args.map{case (n, v) ⇒ s"$n=$v"}.mkString(" ")
	//		    s"$name $args2}"
	//		  }
	//		}
	//		Proxy.test = 1
	//		Proxy.test
	//	}
	//	val r = scala.util.Try{ A.eval$() }
	//	oprintln(r)
	//	r must not be empty
	//}

	def desugar = {

		@ScalaKata object A {
			desugar2 {
				List(1)
				List(1)
			}
		}

		val res = A.eval$() 
		res must not be empty
	}
}
