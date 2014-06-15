package com.scalakata.backend

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

	val route = {
		post {
			path("eval") {
				entity(as[EvalRequest]) { request =>
					val EvalRequest(code) = request

					complete(EvalResponse(List(
						Instrumentation(1, code)
					), List(
						CompilationInfo("cool", 1, Info)
					), false, None))
				}
			} ~
			path("completion") {
				entity(as[CompletionRequest]) { request =>
					val CompletionRequest(code, pos) = request

					complete(List(
						CompletionResponse("map", "(a->b) -> [a] -> [b]")
					))
				}
			}
		}
	}
}