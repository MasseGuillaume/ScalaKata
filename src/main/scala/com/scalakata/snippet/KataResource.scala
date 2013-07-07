package com.scalakata
package snippet

import model.Kata

import net.liftweb._
import net.liftweb.http.{S, Templates}
import sitemap._
import sitemap.Loc._
import common._
import util.Helpers._

import org.bson.types.ObjectId

object KataResource {
  val menu = Menu.param[Box[Kata]](
    "Kata", "Kata",
    findKata,
    encodeKata _ ) / * >>
    Loc.Stateless >>
    Template( () => Templates(List("index")).get
  )
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
}

class KataResource(kata:Box[Kata]) extends DispatchLocSnippets {
  def dispatch = { case "render" => render }
  def render = {
    "@code *" #> kata.map(_.code.is)
  }
}