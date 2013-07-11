package com.scalakata
package service

import model._

import net.liftweb._
import json.JsonAST.JString
import http.rest.RestHelper
import net.liftweb.http._
import util.Helpers._
import net.liftweb.json.JsonAST.JString

object CompileService extends RestHelper {
	serve( "api" :: Nil prefix {
		case "scala" :: Nil JsonPost json -> _ => {
      val JString(code) = json \ "code"
      val (result, newKata) = runAndSave(code)

      JsonResponse(
        JsonPrinter(result, newKata),
        List("Access-Control-Allow-Origin" -> "*"),
        List(),
        ResponseType.forResult(result)
      )
    }
		// case "haskell" :: Nil JsonPost json -> _ => {}
	})

  val ForHtml: LiftRules.DispatchPF = {
    case req @ Req( "api" :: "scala" :: Nil, _, PostRequest ) => {
      req.param("code").map(c => {
        val (result, newKata) = runAndSave(c)
        PlainTextResponse(PlainTextPrinter(result,newKata))
      })
    }
  }

  private def serverScalaVersion =
    scala.tools.nsc.Properties.versionString.split("version ").drop(1).take(1).head

  private def runAndSave(code:String) = {
    val result = ScalaEval(code)
    val newKata = Kata.createRecord.code(code).scalaVersion(serverScalaVersion).save

    (result, newKata)
  }
}