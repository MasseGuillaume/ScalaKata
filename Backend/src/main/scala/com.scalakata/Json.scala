package com.scalakata

import play.api.libs.json._
import play.api.libs.functional.syntax._

import spray.http._
import spray.httpx.marshalling.Marshaller

object FromJson {
	implicit val evalrequest = Json.reads[EvalRequest]
	implicit val completionrequest = Json.reads[CompletionRequest]
}

object ToJson {
	implicit val instrumentation = Json.writes[Instrumentation]
	
	implicit val severity = Writes[Severity] { s_ =>
		val s = s_ match {
			case Error => "error"
			case Warning => "warning"
			case Info => "info"
		}
		JsString(s)
	}

	implicit val compilationinfo = Json.writes[CompilationInfo]
	implicit val runtimeerror = Json.writes[RuntimeError]
	implicit val evalresponse = Json.writes[EvalResponse]
	implicit val completionresponse = Json.writes[CompletionResponse]
}

// implicit val EvalRequestMarshaller = 
// 	Marshaller.of[EvalRequest](`application/json`) { (value, contentType, ctx) =>
// 		ctx.marshalTo(HttpEntity(contentType, string))
// 	}