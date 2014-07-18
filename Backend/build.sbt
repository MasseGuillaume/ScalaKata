offline := true

name := "backend"

organization := "com.scalakata"

offline := true

version := "0.3.0-SNAPSHOT"

scalaVersion := "2.11.2-SNAPSHOT"

Revolver.settings

resolvers ++= Seq(
	"spray repo" at "http://repo.spray.io",
	"typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
	"masseguillaume" at "http://dl.bintray.com/content/masseguillaume/maven",
	Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
	"com.scalakata" %% "eval" % "0.3.0-SNAPSHOT" % "provided",
	"com.scalakata" % "frontend" % "0.3.0-SNAPSHOT" % "provided",
	"io.spray" %% "spray-can" % "1.3.1-20140423",
	"io.spray" %% "spray-routing" % "1.3.1-20140423",
	"com.typesafe.akka" %% "akka-actor" % "2.3.3",
	"com.typesafe.play" %% "play-json" % "2.4-2014-06-14-ea7daf3"
)

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(bintrayPublishSettings:_*)