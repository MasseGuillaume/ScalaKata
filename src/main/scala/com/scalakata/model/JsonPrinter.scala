package com.scalakata.model

import net.liftweb.json._
import JsonAST._
import JsonDSL._

import scala.reflect.internal.util.Position

object JsonPrinter {
  def print(result: EvalResult): JValue = {
    result match {
      case Compile(result,console) => ("result" -> result) ~ ("console" -> console)
      case CompileError(errors) => {
        val jErrors: JValue = errors.map { case ( position, message, severity ) => {
          val severityLabel = severity match {
            case 0 => "info"
            case 1 => "warning"
            case 2 => "error"
          }

          val line = position.line - 3

          val tabCount = position.inUltimateSource( position.source ).lineContent.count( _ == '\t' )
          val column = ( position.column - tabCount * Position.tabInc )

          (
            ("line" -> line) ~
              ("column" -> column) ~
              ("message" -> message) ~
              ("severity" -> severityLabel)
            )

        }}
        "errors" -> jErrors
      }
      case EvalTimeout(timeout) => ("error" -> s"computation cannot exceed $timeout")
      case SecurityError(error) => ("error" -> s"security error: $error")
      case UnknownError(cause) => ("error" -> s"unknown error: $cause")
    }
  }
}