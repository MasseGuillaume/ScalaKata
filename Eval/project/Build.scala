import sbt._
import Keys._
import com.typesafe.sbt.SbtGit._
import sbtbuildinfo.Plugin._

object Settings {
	lazy val default = 
		Project.defaultSettings ++
		bintray.Plugin.bintrayPublishSettings ++
		versionWithGit ++ Seq(
			offline := true,
			organization := "com.scalakata",
			git.baseVersion := "0.1.0",
			scalaVersion := "2.11.2-SNAPSHOT",
			licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
			scalacOptions += "-Yrangepos",
			libraryDependencies ++= Seq(
				"org.scala-lang" % "scala-compiler" % scalaVersion.value,
				"org.specs2" %% s"specs2" % "2.3.12" % "test"
			),
			resolvers ++= Seq(
				Resolver.sonatypeRepo("releases"),
				Resolver.sonatypeRepo("snapshots")
			),
			addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-SNAPSHOT" cross CrossVersion.full)
		)
}

object EvalBuild extends Build {
	import Settings._

	lazy val macro = Project(
		id = "macro",
		base = file("macro"),
		settings = default ++ Seq(
			name := "macro",
			libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
		)
	)

	lazy val compile = Project(
		id = "compile",
		base = file("compile"),
		settings = default ++ buildInfoSettings ++ Seq(
			name := "eval",
			sourceGenerators in Compile <+= buildInfo,
			buildInfoKeys := Seq[BuildInfoKey](
				BuildInfoKey.map((dependencyClasspath in Compile)){ case (k, v) ⇒ k -> v.map(_.data) },
				BuildInfoKey.map((exportedProducts in Runtime in macro)){ case (k, v) ⇒ k -> v.map(_.data) },
				(scalacOptions in Compile)
			),
			buildInfoPackage := "com.scalakata.eval.sbt",
			parallelExecution in Test := false
		)
	)
}