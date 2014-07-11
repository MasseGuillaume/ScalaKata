sbtPlugin := true

name := "plugin"

organization := "com.scalakata"

versionWithGit

git.baseVersion := "0.1"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(bintrayPublishSettings:_*)

scalaVersion := "2.10.4"