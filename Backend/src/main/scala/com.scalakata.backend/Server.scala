package com.scalakata.backend

import com.scalakata.eval._

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot {
	def main(args: Array[String]) = {
		val artifacts :: host :: port :: scalacOptions = args.to[List]
		val compiler = new Compiler(artifacts, scalacOptions)

		implicit val system = ActorSystem("scalakata-system")
		val service = system.actorOf(Props(classOf[ScalaKataActor], artifacts, scalacOptions), "scalakata-service")
		IO(Http) ! Http.Bind(service, host, port.toInt)
	}
}