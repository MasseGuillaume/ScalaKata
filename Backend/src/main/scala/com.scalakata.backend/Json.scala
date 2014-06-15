package com.scalakata.backend

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._

import spray.http._
import spray.http.ContentTypes._
import spray.http.ContentTypeRange._

import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller


object Request {
	val json = ContentTypeRange(MediaTypes.`application/json`, HttpCharsets.`UTF-8`)

	implicit private val evalrequest = Json.reads[EvalRequest]
	implicit val EvalRequestUnmarshaller =
		Unmarshaller.delegate[String, EvalRequest](json) { 
			data => fromJson[EvalRequest](Json.parse(data)).get
		}

	implicit private val completionrequest = Json.reads[CompletionRequest]
	implicit val CompletionRequestUnmarshaller =
		Unmarshaller.delegate[String, CompletionRequest](json) {
			data => fromJson[CompletionRequest](Json.parse(data)).get
		}
}

object Response {
	implicit private val instrumentation = Json.writes[Instrumentation]
	implicit private val severity = Writes[Severity] { s_ =>
		val s = s_ match {
			case Error => "error"
			case Warning => "warning"
			case Info => "info"
		}
		JsString(s)
	}
	implicit private val compilationinfo = Json.writes[CompilationInfo]
	implicit private val runtimeerror = Json.writes[RuntimeError]

	implicit private val evalresponse = Json.writes[EvalResponse]
	implicit val EvalResponseMarshaller = 
		Marshaller.of[EvalResponse](`application/json`) { (eval, contentType, ctx) =>
			ctx.marshalTo(HttpEntity(contentType, toJson(eval).toString))
		}

	implicit private val completionresponse = Json.writes[CompletionResponse]
	implicit val CompletionResponseMarshaller = 
		Marshaller.of[List[CompletionResponse]](`application/json`) { (eval, contentType, ctx) =>
			ctx.marshalTo(HttpEntity(contentType, toJson(eval).toString))
		}
}