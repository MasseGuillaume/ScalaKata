import sbt._
import Keys._
import sbtbuildinfo.Plugin._

object Settings {
	lazy val default = 
		Project.defaultSettings ++
		bintray.Plugin.bintraySettings ++
		bintray.Plugin.bintrayPublishSettings ++
		Seq(
			organization := "com.scalakata",
			scalaVersion := "2.11.2-SNAPSHOT",
			version := "0.2.0-SNAPSHOT",
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

	lazy val classPathtest = Project(
		id = "classpathTest",
		base = file("classPathtest"),
		settings = default ++ Seq(
			/* dont */ publish := { }
		)
	)

	lazy val compile = Project(
		id = "compile",
		base = file("compile"),
		settings = default ++ buildInfoSettings ++ Seq(
			name := "eval",
			sourceGenerators in Test <+= buildInfo,
			buildInfoKeys := Seq[BuildInfoKey](
				BuildInfoKey.map((fullClasspath in classPathtest in Compile)){ case (k, v) ⇒ k -> v.map(_.data) },
				BuildInfoKey.map((exportedProducts in Runtime in macro)){ case (k, v) ⇒ k -> v.map(_.data) },
				(scalacOptions in Compile)
			),
			buildInfoPackage := "com.scalakata.eval.sbt",
			parallelExecution in Test := false
		)
	)
}