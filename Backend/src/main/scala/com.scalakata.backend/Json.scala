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

	implicit private val orderedRender = new Writes[OrderedRender]{
		def writes(s: OrderedRender) = {
			val res:Seq[JsValue] =
				s.map { case ((rs, re), renders) ⇒
					JsArray(Seq(
						JsArray(Seq(JsNumber(rs), JsNumber(re))),
						JsArray(renders.map(rendertype.writes))
					))
				}
			JsArray(res)
		}
	}


	implicit private val expression = new Writes[List[Expression]] {
		def writes(xs: List[Expression]) = JsArray(xs.map(rendertype.writes))
	}

	implicit private val rendertype: Writes[Render] = new Writes[Render] {
		def writes(s: Render) = {
			def wrap(tpe: String, value: String) = wrap2(tpe, JsString(value))
			def wrap2(tpe: String, value: JsValue) =
				JsObject(Seq("type" -> JsString(tpe), "value" -> value))

			implicit val tuples = new Writes[(String, Int)] {
				def writes(t: (String, Int)) = JsArray(Seq(JsString(t._1), JsNumber(t._2)))
			}
			val res =
				s match {
					case Html(v, h) ⇒ wrap2("html", toJson((v, h)))
					case Html2(v) ⇒ wrap2("html2", toJson(v))
					case Latex(v) ⇒ wrap("latex", v)
					case Markdown(v) ⇒ wrap("markdown", v)
					case Markdown2(v) ⇒ wrap("markdown2", v)
					case EString(v) ⇒ wrap("string", v)
					case Other(v) ⇒ wrap("other", v)
					case Block(cs) ⇒ wrap2("block", toJson(cs))
					case Steps(ss) ⇒ wrap2("steps", toJson(ss))
				}
			toJson(res)
		}
	}

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
