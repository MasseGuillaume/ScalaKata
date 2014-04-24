package com.scalakata

import sbt._
import Keys._

object Scalakata extends Plugin {
  override lazy val settings = Seq(
    commands ++= Seq(
      sample
    )
  )

  lazy val sample = Command.command("sampleCommand") { state =>
    println("Hello SBT World!")
    state
  }
}

