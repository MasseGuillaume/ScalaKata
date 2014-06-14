sbtPlugin := true

name := "plugin"

organization := "com.scalakata"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	"io.spray" %% "spray-httpx" % "1.3.1",
	"io.argonaut" %% "argonaut" % "6.0.4"
)