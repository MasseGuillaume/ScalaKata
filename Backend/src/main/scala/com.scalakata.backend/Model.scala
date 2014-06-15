package com.scalakata.backend

sealed trait Severity
final case object Info extends Severity
final case object Warning extends Severity
final case object Error extends Severity

case class Instrumentation(
	line: Int,
	result: String
)

case class CompilationInfo(
	message: String,
	position: Int,
	severity: Severity
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
	infos: List[CompilationInfo],
	timeout: Boolean,
	runtimeError: Option[RuntimeError]
)

case class CompletionRequest(
	code: String,
	position: Int
)

case class CompletionResponse(
	name: String,
	signature: String
)