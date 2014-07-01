package com.scalakata.backend

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {

	implicit val system = ActorSystem("on-spray-can")

	val service = system.actorOf(Props[ScalaKataActor], "scalakata-service")

	IO(Http) ! Http.Bind(service, "localhost", port = 8080)
}