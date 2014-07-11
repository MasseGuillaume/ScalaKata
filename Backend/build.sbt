import com.typesafe.sbt.SbtGit._

offline := true

name := "backend"

organization := "com.scalakata"

versionWithGit

git.baseVersion := "0.1"

scalaVersion := "2.11.2-SNAPSHOT"

offline := true

Revolver.settings

resolvers ++= Seq(
	"spray repo" at "http://repo.spray.io",
	"typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
	Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
	"com.scalakata" %% "eval" % "0.1.0-20140711T185007" % "provided",
	"com.scalakata" % "frontend" % "0.1-20140711T152757" % "provided",
	"io.spray" %% "spray-can" % "1.3.1-20140423",
	"io.spray" %% "spray-routing" % "1.3.1-20140423",
	"com.typesafe.akka" %% "akka-actor" % "2.3.3",
	"com.typesafe.play" %% "play-json" % "2.4-2014-06-14-ea7daf3"
)

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(bintrayPublishSettings:_*)