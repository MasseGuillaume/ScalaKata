package com.scalakata

import akka.actor._
import spray.routing.HttpService

class ScalaKataActor extends Actor with ScalaKata {
	def actorRefFactory = context
	def receive = runRoute(route)
}

trait ScalaKata extends HttpService {
	implicit def executionContext = actorRefFactory.dispatcher

	val route = {
		post {
			path("eval") {
				content(as[EvalRequest]) { request =>
					
				}
			} ~
			path("completion") {
				content(as[CompletionRequest]) { request =>

				}
			}
		}
	}
}