package com.scalakata.backend

import com.scalakata.eval._

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import spray.can.Http

object Boot {
	def main(args: Array[String]) = {
		val readyPort :: artifacts :: host :: port :: codePrelude :: code :: scalacOptions = args.to[List]

		val compiler = new Compiler(artifacts, scalacOptions)

		implicit val system = ActorSystem("scalakata-system")
		val service = system.actorOf(Props(
			classOf[ScalaKataActor], artifacts, codePrelude, code, scalacOptions
		), "scalakata-service")

		import scala.concurrent.duration._
		import akka.pattern.ask
		implicit val bindingTimeout = Timeout(1.second)
		import system.dispatcher
		(IO(Http) ? Http.Bind(service, host, port.toInt)) onSuccess {
			case _: Http.Bound => {
				val ready = new java.net.Socket(host, readyPort.toInt)
				ready.sendUrgentData(0)
				ready.close()
			}
		}
	}
}
