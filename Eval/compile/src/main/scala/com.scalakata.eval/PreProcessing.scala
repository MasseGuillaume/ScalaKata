package com.scalakata.eval

object PreProcessing {
    import scala.reflect.runtime.{currentMirror => cm}
    import scala.reflect.runtime.universe._
    import scala.tools.reflect.ToolBox
    val tb = cm.mkToolBox()

    def extractValueClasses(code: String): (Int, String) = {
        tb.parse(code) match {
            case q"object Instrumented{ ..$elems }" =>

                val (valueClasses, normals) = elems.partition {
                    case vc @ ClassDef(_, _, _, Template(List(Ident(TypeName("AnyVal"))), _, _ )) => true
                    case _ => false
                }
                val nl = System.lineSeparator
                val svc = valueClasses.map(vc => showCode(vc)).mkString(nl)
                val pre = "@com.scalakata.eval.ScalaKata object Instrumented {"
                val I = showCode(q"""
                    $pre {
                        object Inner {
                          ..$normals
                        }
                    }
                    """
                )
                (svc.length + pre.length, svc + nl + I)
        }
    }
}