import bintray.Keys._

sbtPlugin := true

offline := true

name := "plugin"

organization := "com.scalakata"

version := "0.3.0-SNAPSHOT"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(bintrayPublishSettings:_*)

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

scalaVersion := "2.10.4"