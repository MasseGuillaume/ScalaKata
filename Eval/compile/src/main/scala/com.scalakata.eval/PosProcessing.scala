package com.scalakata.eval

object PostProcessing {
	def apply(instr: Eval.Instrumentation) = {
        instr.map{ case ((start, end), value) ⇒
          val (v, xml) = value match {
            case a: String ⇒ {
              val res = 
                if (a.lines.size > 1) {
                  val quote = "\"\"\""
                  val nl = System.lineSeparator
                  quote + nl + a + nl + quote
                }
                else {
                  val quote = "\""
                  quote + a + quote
                }
              (res, false)
            }
            case xml: scala.xml.Elem ⇒
              (xml.toString, true)
            case other ⇒ (other.toString, false)
          }
          Instrumentation(v, xml, start, end)
        }.to[List]
	}
}