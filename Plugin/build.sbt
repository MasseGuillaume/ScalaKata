sbtPlugin := true

name := "plugin"

organization := "com.scalakata"

versionWithGit

git.baseVersion := "0.1"

scalaVersion := "2.10.4"

resolvers ++= Seq(
	"spray repo" at "http://repo.spray.io",
	"typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
	Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
	"io.spray" %% "spray-client" % "1.3.1",
	// ? "com.typesafe.akka" %% "akka-actor" % "2.3.3",
	"com.typesafe.play" %% "play-json" % "2.4-2014-06-14-ea7daf3"
)