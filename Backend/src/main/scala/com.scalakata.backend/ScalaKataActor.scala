package com.scalakata
package backend

import eval._

import akka.actor._
import spray.routing.HttpService
import spray.http._
import spray.util._

class ScalaKataActor(
	override val artifacts: String,
	override val scalacOptions: Seq[String],
	override val security: Boolean) extends Actor with ScalaKata {

	def actorRefFactory = context
	def receive = runRoute(route)
}

trait ScalaKata extends HttpService {
	val artifacts: String
	val scalacOptions: Seq[String]
	val security: Boolean

	implicit def executionContext = actorRefFactory.dispatcher

	import Request._
	import Response._

	lazy val compiler = new Compiler(artifacts, scalacOptions, security)

	val redirectCodebrew = hostName { hn ⇒
		if(hn == "codebrew.io") redirect("www.scalakata.com", StatusCodes.PermanentRedirect)
		else getFromResource("assets/index.html")
	}

	val route = {
		path("eval") {
			post {
				entity(as[EvalRequest]) { request ⇒
					val EvalRequest(code) = request
					complete(compiler.insight(code))
				}
			}
		} ~
		path("completion") {
			post {
				entity(as[CompletionRequest]) { request ⇒
					val CompletionRequest(code, pos) = request
					complete(compiler.autocomplete(code, pos))
				}
			}
		} ~
		path("typeAt") {
			post {
				entity(as[TypeAtRequest]) { request ⇒
					val TypeAtRequest(code, pos) = request
					complete(compiler.typeAt(code, pos, pos))
				}
			}
		} ~
		path("echo") {
			post {
				formFields('code){ code ⇒
					respondWithHeader(HttpHeaders.RawHeader("X-XSS-Protection", "0")) {
						complete(HttpEntity(ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`),
					 		HttpData(code)))
					}
				}
			}
		} ~
		pathSingleSlash {
			redirectCodebrew
    } ~
		path("assets" / Rest) { path ⇒
			getFromResource(s"assets/$path")
		} ~
		path("kata" / "scala" / Rest) { path ⇒
			getFromResource(s"scala/$path")
		} ~
		path(Rest) { path ⇒
			redirectCodebrew
		}
	}
}
