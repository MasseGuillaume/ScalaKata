name := "eval"

organization := "com.scalakata"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](
	BuildInfoKey.map((dependencyClasspath in Compile)){ case (k, v) => k -> v.map(_.data) }
)

buildInfoPackage := "com.scalakata.eval.sbt"