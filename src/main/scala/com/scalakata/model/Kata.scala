package com.scalakata
package model

import service.KataMongo

import net.liftweb._
import record.field._
import mongodb.record._
import field._

class Kata extends MongoRecord[Kata] with MongoId[Kata] {
  def meta = Kata

  object code extends StringField(this, 8191)
  object scalaVersion extends StringField(this, 127)
}

object Kata extends Kata with MongoMetaRecord[Kata] {
  override def mongoIdentifier = KataMongo
}