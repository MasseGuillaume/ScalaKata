import sbt._
import Keys._
import com.typesafe.sbt.SbtGit._
import sbtbuildinfo.Plugin._

object Settings {
	lazy val default = 
		Project.defaultSettings ++
		versionWithGit ++ Seq(
			organization := "com.scalakata",
			git.baseVersion := "0.1.0",
			scalaVersion := "2.11.1",
			libraryDependencies += "org.specs2" %% s"specs2" % "2.3.12" % "test",
			resolvers += Resolver.sonatypeRepo("releases"),
			addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full)
		)
}

object EvalBuild extends Build {
	import Settings._

	lazy val macro = Project(
		id = "macro",
		base = file("macro"),
		settings = default ++ Seq(
			name := "macro",
			scalacOptions += "-Yrangepos",
			libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
			libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)
		)
	)



	lazy val compile = Project(
		id = "compile",
		base = file("compile"),
		settings = default ++ buildInfoSettings ++ Seq(
			name := "eval",
			libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _),
			sourceGenerators in Compile <+= buildInfo,
			buildInfoKeys := Seq[BuildInfoKey](
				BuildInfoKey.map((dependencyClasspath in Compile)){ case (k, v) => k -> v.map(_.data) },
				BuildInfoKey.map((exportedProducts in Runtime in macro)){ case (k, v) => k -> v.map(_.data) },
				(scalacOptions in Compile)
			),
			buildInfoPackage := "com.scalakata.eval.sbt"
		)
	)
}