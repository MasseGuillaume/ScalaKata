package com.scalakata
package backend

import eval._

import akka.actor._
import spray.routing.HttpService

class ScalaKataActor extends Actor with ScalaKata {
	def actorRefFactory = context
	def receive = runRoute(route)
}

trait ScalaKata extends HttpService {
	implicit def executionContext = actorRefFactory.dispatcher

	import Request._
	import Response._

	val compiler = new Compiler

	val route = {
		post {
			path("eval") {
				entity(as[EvalRequest]) { request =>
					val EvalRequest(code) = request
					complete(compiler.insight(code))
				}
			} ~
			path("completion") {
				entity(as[CompletionRequest]) { request =>
					val CompletionRequest(code, pos) = request
					complete(compiler.autocomplete(code, pos))
				}
			}
		}
	}
}