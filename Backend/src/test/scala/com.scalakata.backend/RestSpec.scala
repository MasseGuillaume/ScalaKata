package com.scalakata
package backend

import org.specs2._
import spray.testkit.Specs2RouteTest
import spray.http.StatusCodes._

class RestSpec extends Specification with Specs2RouteTest with ScalaKata { def is = s2"""
  $assets must work
  $assetRoute must handle querystrings
  $resourceRoute must redirect resources to index
  $index must work
"""
  def actorRefFactory = system
  val artifacts: String = ""
  val code: String = ""
  val codePrelude: String = ""
  val scalacOptions: Seq[String] = Seq()

  def assets = {
    Get("/assets/b/main.css") ~> route ~> check {
      responseAs[String] must contain("css")
    }
  }

  def assetRoute = {
    Get("/assets/MathJax/MathJax.js?config=TeX-AMS-MML_HTMLorMML&delayStartupUntil=configured&dummy=.js") ~> route ~> check {
      status mustEqual OK
    }
  }

  def resourceRoute = {
    Get("/a/1.scala") ~> route ~> check {
      responseAs[String] must contain("index")
    }
  }

  def index = {
    Get("/") ~> route ~> check {
      responseAs[String] must contain("index")
    }
  }
}
