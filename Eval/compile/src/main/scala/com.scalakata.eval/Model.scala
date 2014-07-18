package com.scalakata.eval

sealed trait Severity
final case object Info extends Severity
final case object Warning extends Severity
final case object Error extends Severity

case class Instrumentation(
	result: String,
	xml: Boolean,
	start: Int,
	end: Int
)

case class CompilationInfo(
	message: String,
	position: Int
)

case class RuntimeError(
	message: String,
	line: Int
)

case class EvalRequest(
	code: String
)

case class EvalResponse(
	insight: List[Instrumentation],
	infos: Map[Severity, List[CompilationInfo]],
	timeout: Boolean,
	runtimeError: Option[RuntimeError]
)

object EvalResponse {
	def empty = EvalResponse(Nil, Map.empty, false, None)
}

case class TypeAtRequest(
	code: String,
	position: Int
)

case class TypeAtResponse(
	tpe: String
)

case class CompletionRequest(
	code: String,
	position: Int
)

case class CompletionResponse(
	name: String,
	signature: String
)