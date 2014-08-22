package com.scalakata
package backend

import eval._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._

import spray.http._
import spray.http.ContentTypes._
import spray.http.ContentTypeRange._

import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller

case class Code(prelude: String, code: String)

object Request {
	val json = ContentTypeRange(MediaTypes.`application/json`, HttpCharsets.`UTF-8`)

	implicit private val evalrequest = Json.reads[EvalRequest]
	implicit val EvalRequestUnmarshaller =
		Unmarshaller.delegate[String, EvalRequest](json) {
			data ⇒ fromJson[EvalRequest](Json.parse(data)).get
		}

	implicit private val completionrequest = Json.reads[CompletionRequest]
	implicit val CompletionRequestUnmarshaller =
		Unmarshaller.delegate[String, CompletionRequest](json) {
			data ⇒ fromJson[CompletionRequest](Json.parse(data)).get
		}

	implicit private val typeatrequest = Json.reads[TypeAtRequest]
	implicit val TypeAtRequestUnmarshaller =
		Unmarshaller.delegate[String, TypeAtRequest](json) {
			data ⇒ fromJson[TypeAtRequest](Json.parse(data)).get
		}
}

object Response {
	implicit private val code = Json.writes[Code]

	implicit private val rendertype = new Writes[RenderType] {
		def writes(s: RenderType) = {
			val res =
				s match {
					case RT_Html => "html"
					case RT_Latex => "latex"
					case RT_Markdown => "markdown"
					case RT_String => "string"
					case RT_Block => "block"
					case RT_Other => "other"
				}
			toJson(res)
		}
	}

	implicit private val instrumentation = Json.writes[Instrumentation]

	implicit private val compilationinfo = Json.writes[CompilationInfo]
	implicit private val runtimeerror = Json.writes[RuntimeError]
	implicit private val compilationinfomap = new Writes[Map[Severity,List[CompilationInfo]]] {
		def writes(s: Map[Severity,List[CompilationInfo]]) = {
			val a = s.map{ case (s, cis) ⇒
				val sev = s match {
					case Error ⇒ "error"
					case Warning ⇒ "warning"
					case Info ⇒ "info"
				}
				sev -> toJson(cis)
			}
			toJson(a)
		}
	}

	implicit private val evalresponse = Json.writes[EvalResponse]
	implicit val EvalResponseMarshaller =
		Marshaller.of[EvalResponse](`application/json`) { (eval, contentType, ctx) ⇒
			ctx.marshalTo(HttpEntity(contentType, toJson(eval).toString))
		}

	implicit private val completionresponse = Json.writes[CompletionResponse]
	implicit val CompletionResponseMarshaller =
		Marshaller.of[List[CompletionResponse]](`application/json`) { (eval, contentType, ctx) ⇒
			ctx.marshalTo(HttpEntity(contentType, toJson(eval).toString))
		}

	implicit private val typeatresponse = Json.writes[TypeAtResponse]
	implicit val typeatresponseMarshaller =
		Marshaller.of[Option[TypeAtResponse]](`application/json`) { (eval, contentType, ctx) ⇒
			ctx.marshalTo(HttpEntity(contentType, toJson(eval).toString))
		}

	implicit val CodeMarshaller =
		Marshaller.of[Code](`application/json`) { (eval, contentType, ctx) ⇒
			ctx.marshalTo(HttpEntity(contentType, toJson(eval).toString))
		}
}
