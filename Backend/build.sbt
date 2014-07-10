import com.typesafe.sbt.SbtGit._

name := "backend"

organization := "com.scalakata"

versionWithGit

git.baseVersion := "0.1"

scalaVersion := "2.11.2-SNAPSHOT"

offline := true

Revolver.settings

Revolver.reStartArgs := List(
	"/home/masgui/.ivy2/cache/org.scala-lang/scala-compiler/jars/scala-compiler-2.11.2-SNAPSHOT.jar:/home/masgui/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.11.2-SNAPSHOT.jar:/home/masgui/.ivy2/cache/org.scala-lang/scala-reflect/jars/scala-reflect-2.11.2-SNAPSHOT.jar:/home/masgui/.ivy2/cache/org.scala-lang.modules/scala-xml_2.11/bundles/scala-xml_2.11-1.0.2.jar:/home/masgui/.ivy2/cache/org.scala-lang.modules/scala-parser-combinators_2.11/bundles/scala-parser-combinators_2.11-1.0.1.jar:/home/masgui/.ivy2/local/com.scalakata/macro_2.11/0.1.0-20140710T173600/jars/macro_2.11.jar",
	"localhost", 
	"8080", 
	"-Xplugin:/home/masgui/.ivy2/cache/org.scalamacros/paradise_2.11.2-SNAPSHOT/jars/paradise_2.11.2-SNAPSHOT-2.1.0-SNAPSHOT.jar",
	"-Yrangepos"
)

resolvers ++= Seq(
	"spray repo" at "http://repo.spray.io",
	"typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
	Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
	"com.scalakata" %% "eval" % "0.1.0-20140710T190356",
	"com.scalakata" %% "frontend" % "0.1-20140702T202545",
	"io.spray" %% "spray-can" % "1.3.1-20140423",
	"io.spray" %% "spray-routing" % "1.3.1-20140423",
	"com.typesafe.akka" %% "akka-actor" % "2.3.3",
	"com.typesafe.play" %% "play-json" % "2.4-2014-06-14-ea7daf3"
)