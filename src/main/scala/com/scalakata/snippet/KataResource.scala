package com.scalakata
package snippet

import model.Kata

import net.liftweb._
import http._
import sitemap._
import sitemap.Loc._
import common._
import util.Helpers._

import org.bson.types.ObjectId

object KataResource {
  def menuFor(path: String) = {
    def findKata(id: String): Box[Box[Kata]] = {
      id match {
        case "index" => Full(Empty)
        case _ => {
          for {
            oid <- tryo(new ObjectId(id))
            kata <- Kata.find(oid)
          } yield Full(kata)
        }
      }
    }
    def encodeKata( kata: Box[Kata] ): String =
      kata.map(_._id.toString).getOrElse("")

    if(path == "index") {
      Menu.param[Box[Kata]](
        "Kata Classic", "Kata Classic",
        findKata,
        encodeKata _ ) / * >>
        Loc.Stateless >>
        Template( () => Templates(List(path)).get
      )
    } else {
      Menu.param[Box[Kata]](
        "Kata TDD", "Kata TDD",
        findKata,
        encodeKata _ ) / path / * >>
        Loc.Stateless >>
        Template( () => Templates(List(path)).get
      )
    }
  }

  val classic = menuFor("index")
  val tdd = menuFor("tdd")

  val tddRedirect: LiftRules.DispatchPF = {
    case req @ Req( "tdd" :: Nil, _, GetRequest ) => {
      () => Full(PermRedirectResponse("/tdd/",req))
    }
  }
}

class KataResource(kata:Box[Kata]) extends DispatchLocSnippets {
  def dispatch = { case "render" => render }
  def render = {
    val (code, test) = kata.map(k => {
      if (k.scalaVersion.is != serverScalaVersion ) {
        S.redirectTo(s"http://scala-2-9-2.scalakata.com/${k._id.is}")
      }

      (k.code.is, k.test.is)
    }).getOrElse(("",""))
  
    "@code *" #> code & "@test *" #> test
  }

  def serverScalaVersion =
    scala.tools.nsc.Properties.versionString.split("version ").drop(1).take(1).head
}