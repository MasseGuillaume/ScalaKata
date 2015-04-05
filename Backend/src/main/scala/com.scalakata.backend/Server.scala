package com.scalakata.backend

import com.scalakata.eval._

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration._
import com.typesafe.config.{ ConfigValueFactory, ConfigFactory, Config }


object Boot {
	def main(args: Array[String]) = {
		val (readyPort :: artifacts :: host :: port ::
				 production :: security :: timeoutS :: scalacOptions) = args.to[List]

    val timeout = Duration(timeoutS)

    val config: Config = ConfigFactory.parseString(s"""
      spray {
        can.server {
          idle-timeout = ${timeout.toSeconds + 5}s
          request-timeout = ${timeout.toSeconds + 2}s
        }
      }
    """)

    implicit val system = ActorSystem("scalakata-playground", config)

		val service = system.actorOf(Props(
			classOf[ScalaKataActor], artifacts, scalacOptions, security.toBoolean, timeout
		), "scalakata-service")


		import akka.pattern.ask
		implicit val bindingTimeout = Timeout(5.seconds)
		import system.dispatcher
		(IO(Http) ? Http.Bind(service, host, port.toInt)) onSuccess {
			case _: Http.Bound ⇒ {
				if(!production.toBoolean) {
					val ready = new java.net.Socket(host, readyPort.toInt)
					ready.sendUrgentData(0)
					ready.close()
				}
			}
		}
	}
}
