package com.scalakata.boot

import org.webjars.WebJarAssetLocator

import scala.util.Try

import net.liftweb._
import http._
import util._

object WebJars {
  val locator = new WebJarAssetLocator()
  def serve: Unit = {

    ResourceServer.baseResourceLocation = "META-INF"
    ResourceServer.allow {
      case _ if Try(locator.getFullPath(S.uri.drop("/webjars/".length))).isSuccess => true
    }

    LiftRules.statelessDispatch.prepend(NamedPF("Webjar service") {
      case r@Req(prefix :: tail, suffx, _) if (prefix == "webjars") => {
        val test = Try(locator.getFullPath(S.uri.drop("/webjars/".length))).isSuccess
        val ressource = locator.getFullPath(tail.mkString("/")).split("/").toList.drop(1)
        ResourceServer.findResourceInClasspath(r, ressource)
      }
    })
  }
}
