import bintray.Keys._

sbtPlugin := true

name := "plugin"

organization := "com.scalakata"

version := "0.8.0-SNAPSHOT"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "0.5.2")

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(bintraySettings:_*)

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

scalaVersion := "2.10.4"
