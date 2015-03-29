import sbt._
import Keys._
import sbtbuildinfo.Plugin._

object Settings {
	lazy val default =
		Project.defaultSettings ++
		bintray.Plugin.bintraySettings ++
		Seq(
			organization := "com.scalakata",
			scalaVersion := "2.11.6",
			version := "0.11.0-SNAPSHOT",
			licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
			scalacOptions ++= Seq("-Yrangepos", "-unchecked", "-deprecation", "-feature"),
			libraryDependencies ++= Seq(
				"org.scala-lang" % "scala-compiler" % scalaVersion.value,
				"org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "org.specs2" %% "specs2-core" % "3.1" % "test"
			),
			resolvers += Resolver.sonatypeRepo("releases"),
			addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
		)
}

object EvalBuild extends Build {
	import Settings._

	lazy val macro = Project(
		id = "macro",
		base = file("macro"),
		settings = default ++ Seq(
			name := "macro"
		)
	)

	lazy val classPathtest = Project(
		id = "classpathTest",
		base = file("classPathtest"),
		settings = default ++ Seq(
			/* dont */ publish := { },
			/* dont */ publishLocal := { }
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
	) dependsOn(macro)
}
