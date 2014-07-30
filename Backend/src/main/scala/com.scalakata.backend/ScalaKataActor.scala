package com.scalakata
package backend

import eval._

import akka.actor._
import spray.routing.HttpService

class ScalaKataActor(
	override val artifacts: String,
	override val codePrelude: String,
	override val code: String,
	override val scalacOptions: Seq[String]) extends Actor with ScalaKata {

	def actorRefFactory = context
	def receive = runRoute(route)
}

trait ScalaKata extends HttpService {
	val artifacts: String
	val scalacOptions: Seq[String]
	val codePrelude: String
	val code: String

	implicit def executionContext = actorRefFactory.dispatcher

	import Request._
	import Response._

	val compiler = new Compiler(artifacts, scalacOptions)

	val route = {
		path("initialCode") {
			get {
				complete(Seq(codePrelude, code))
			}
		} ~
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
		pathSingleSlash {
          getFromResource("index.html")
        } ~
		path(Rest) { path ⇒
			getFromResource(path)
		}
	}
}
