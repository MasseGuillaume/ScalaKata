package com.scalakata
package service

import model._

import net.liftweb._
import http.rest.RestHelper
import http._
import util.Helpers._
import json.JsonAST.JString

object CompileService extends RestHelper {

  private val headers = List("Access-Control-Allow-Origin" -> "*")
  private val nl = sys.props("line.separator")

//// case "haskell" :: Nil JsonPost json -> _ => {}
  def serve: LiftRules.DispatchPF = {
    case req @ Req( "api" :: "scala" :: Nil, _, PostRequest ) => {

      def serverScalaVersion =
        scala.tools.nsc.Properties.versionString.split("version ").drop(1).take(1).head

      def runAndSave(code:String, test:String) = {
        val result = ScalaEval(code + nl + test)
        val newKata = Kata.createRecord.code(code).test(test).scalaVersion(serverScalaVersion).save

        (result, newKata)
      }

      def plain = {
        req.param("code").map(code => {
          val test = req.param("test").getOrElse("")
          val (result, newKata) = runAndSave(code,test)
          PlainTextResponse(
            PlainTextPrinter(result,newKata),
            headers,
            ResponseType.forResult(result)
          )
        })
      }

      def json = {
        req.json.map(j => {
          val JString(code) = j \ "code"
          val JString(test) = j \ "test"
          val (result, newKata) = runAndSave(code, test)

          JsonResponse(
            JsonPrinter(result, newKata),
            headers,
            List(),
            ResponseType.forResult(result)
          )
        })
      }

      if(json.isEmpty) {
        plain
      } else {
        json
      }
    }
  }
}