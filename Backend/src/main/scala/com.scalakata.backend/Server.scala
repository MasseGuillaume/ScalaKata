package com.scalakata.backend

import com.scalakata.eval._

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot {
	def main(args: Array[String]) = {
		val readyPort :: artifacts :: host :: port :: initialCommands :: scalacOptions = args.to[List]
		
		val compiler = new Compiler(artifacts, scalacOptions)

		implicit val system = ActorSystem("scalakata-system")
		val service = system.actorOf(Props(
			classOf[ScalaKataActor], artifacts, initialCommands, scalacOptions
		), "scalakata-service")
		IO(Http) ! Http.Bind(service, host, port.toInt)

		val ready = new java.net.Socket(host, readyPort.toInt)
		ready.sendUrgentData(0)
		ready.close()
	}
}