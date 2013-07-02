package com.scalakata.boot

import org.webjars.{MultipleMatchesException, WebJarAssetLocator}

import scala.util.Try

import net.liftweb._
import http._
import util._
import common._


object WebJars {
  val locator = new WebJarAssetLocator()
  def serve: Unit = {

    ResourceServer.baseResourceLocation = "META-INF"
    ResourceServer.allow {
      case _ => true
    }

    LiftRules.statelessDispatch.prepend(NamedPF("Webjar service") {
      case r@Req(prefix :: tail, suffx, _) if (prefix == "webjars") => {
        try {
          val path = locator.getFullPath(S.uri.drop("/webjars/".length)).split("/").drop(1).toList
          ResourceServer.findResourceInClasspath(r, path)
        } catch {
          case _: IllegalArgumentException => () => Empty
          case _: MultipleMatchesException => () => Empty
        }
      }
    })
  }
}
