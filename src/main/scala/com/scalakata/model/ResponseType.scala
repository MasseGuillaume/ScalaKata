package com.scalakata.model

object ResponseType {
  def forResult(result: EvalResult): Int = {
    result match {
      case its: Compile => 200
      case its: CompileError => 200
      case its: SecurityError => 403
      case its: EvalTimeout => 408
      case its: UnknownError => 500
      case other => 500
    }
  }
}
