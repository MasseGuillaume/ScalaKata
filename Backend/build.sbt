name := "backend"

organization := "com.scalakata"

version := "0.7.0"

scalaVersion := "2.11.2"

Revolver.settings

resolvers ++= Seq(
	"spray repo" at "http://repo.spray.io",
	"typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
	"masseguillaume" at "http://dl.bintray.com/content/masseguillaume/maven"
)

libraryDependencies ++= Seq(
	"com.scalakata" %% "eval" % "0.7.0",
	"com.scalakata" % "frontend" % "0.7.0",
	"io.spray" %% "spray-can" % "1.3.1-20140423",
	"io.spray" %% "spray-routing" % "1.3.1-20140423",
	"io.spray" %% "spray-testkit" % "1.3.1-20140423" % "test",
	"org.specs2" %% s"specs2" % "2.3.12" % "test",
	"com.typesafe.akka" %% "akka-actor" % "2.3.3",
	"com.typesafe.play" %% "play-json" % "2.4-2014-06-14-ea7daf3"
)

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(bintraySettings:_*)
