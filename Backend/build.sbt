import com.typesafe.sbt.SbtGit._

name := "backend"

organization := "com.scalakata"

versionWithGit

scalaVersion := "2.11.1"

Revolver.settings

resolvers ++= Seq(
	"spray repo" at "http://repo.spray.io",
	"typesafe releases" at "http://repo.typesafe.com/typesafe/releases"
)

libraryDependencies ++= Seq(
	"com.scalakata"
	"io.spray" %% "spray-can" % "1.3.1-20140423",
	"io.spray" %% "spray-routing" % "1.3.1-20140423",
	"com.typesafe.akka" %% "akka-actor" % "2.3.3",
	"com.typesafe.play" %% "play-json" % "2.4-2014-06-14-ea7daf3"
)