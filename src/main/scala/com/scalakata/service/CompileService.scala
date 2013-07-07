package com.scalakata
package service

import model._

import net.liftweb._
import json.JsonAST.JString
import http.rest.RestHelper
import http.JsonResponse
import util.Helpers._

object CompileService extends RestHelper {
	serve( "api" :: Nil prefix {
		case "scala" :: Nil JsonPost json -> _ => {
      val JString(code) = json \ "code"
      val result = ScalaEval(code)
      val newKata = Kata.createRecord.code(code).save

      JsonResponse(
        JsonPrinter.print(result, newKata),
        List("Access-Control-Allow-Origin" -> "*"),
        List(),
        ResponseType.forResult(result)
      )
    }
		// case "haskell" :: Nil JsonPost json -> _ => {}
	})
}
