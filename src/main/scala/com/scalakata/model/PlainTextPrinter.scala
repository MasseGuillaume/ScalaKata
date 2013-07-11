package com.scalakata.model

import net.liftweb.http.S

object PlainTextPrinter {
  def apply(result: EvalResult, newKata: Kata): String = result match {
    case Compile(result, console) => {
      s"""
        |console: $console
        |
        |result: $result
        |
        |see: ${S.hostName}/${newKata._id.get.toString}
      """.stripMargin
    }
    case CompileError(errors) => {
      errors.map{case(Error(line, column, message, severity)) => {
        s"$severity line: $line col: $column $message"
      }}.mkString("\n")
    }
    case RuntimeError(cause) => s"error $cause"
    case EvalTimeout(timeout) => s"computation cannot exceed $timeout"
    case SecurityError(error) => s"security error: $error"
  }
}