package com.scalakata.model

import net.liftweb.json._
import JsonAST._
import JsonDSL._

import scala.reflect.internal.util.Position
import net.liftweb.http.S

object JsonPrinter {
  def apply(result: EvalResult, newKata: Kata): JValue = {
    val jval: JValue = result match {
      case Compile(result,console) => ("result" -> result) ~ ("console" -> console)
      case CompileError(errors) => {
        "errors" -> errors.map{ e =>
          ("line" -> e.line) ~
          ("column" -> e.column) ~
          ("message" -> e.message) ~
          ("severity" -> e.severity)
        }
      }
      case RuntimeError(cause) => ("error" -> cause)
      case EvalTimeout(timeout) => ("error" -> s"computation cannot exceed $timeout")
      case SecurityError(error) => ("error" -> s"security error: $error")
    }
    jval.merge(("id" -> newKata._id.get.toString): JValue)
  }
}