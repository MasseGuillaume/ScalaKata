name := "eval"

organization := "com.scalakata"

versionWithGit

git.baseVersion := "0.1.0"

scalaVersion := "2.11.1"

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)

libraryDependencies += "org.specs2" %% s"specs2" % "2.3.12" % "test"

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](
	BuildInfoKey.map((dependencyClasspath in Compile)){ case (k, v) => k -> v.map(_.data) }
)

buildInfoPackage := "com.scalakata.eval.sbt"

initialCommands in console := """
import com.scalakata.eval.Compiler
val c = new Compiler
"""