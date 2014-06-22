sbtPlugin := true

name := "plugin"

organization := "com.scalakata"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	"io.spray" %% "spray-client" % "1.3.1-20140423",
	// ? "com.typesafe.akka" %% "akka-actor" % "2.3.3",
	"com.typesafe.play" %% "play-json" % "2.4-2014-06-14-ea7daf3"
)