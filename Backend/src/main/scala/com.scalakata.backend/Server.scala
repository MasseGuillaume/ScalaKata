package com.scalakata.backend

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {
	def main(args: Seq[String]) = {
		val Seq(artifacts, host, port, scalacOptions) = args
		implicit val system = ActorSystem("scalakata-system")
		val service = system.actorOf(Props(classOf[ScalaKataActor], artifacts, scalacOptions), "scalakata-service")
		IO(Http) ! Http.Bind(service, host, port)
	}
}