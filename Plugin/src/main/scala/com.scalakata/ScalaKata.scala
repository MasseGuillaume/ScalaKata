package com.scalakata

import java.awt.Desktop

import sbt._
import Def.Initialize
import Keys._
import Attributed.data

import java.net.URL
import java.io.File

import spray.revolver.Actions
import spray.revolver.RevolverPlugin.Revolver

object Scalakata extends Plugin {

	lazy val Kata = config("kata") extend(Runtime)
	lazy val Backend = config("backend")

	lazy val openBrowser = TaskKey[Unit]("open-browser", "task to open browser to kata url")
	lazy val readyPort = SettingKey[Int]("ready-port", "port to send ready command")
	lazy val kataUri = SettingKey[URI]("kata-uri", "uri to scala kata")
	lazy val initialCode = SettingKey[(String, String)]("initial-code", "initial code in the kata")
	lazy val startArgs = TaskKey[Seq[String]]("start-args",
    	"The arguments to be passed to the applications main method when being started")

	lazy val test = Project(
		id = "test",
		base = file("."),
		settings = kataSettings
	)
	lazy val scalaKataVersion = "0.6.0-SNAPSHOT"
	val start = "kstart"

	lazy val kataAutoStart =
		onLoad in Global := {
			((s: State) => { start :: s }) compose (onLoad in Global).value
		}

	lazy val kataSettings =
		Project.defaultSettings ++
		addCommandAlias(start, ";backend:reStart ;backend:openBrowser; ~ backend:copyResources") ++
		addCommandAlias("kstop", "backend:reStop") ++
		addCommandAlias("krestart", ";backend:reStop ;backend:reStart") ++
		inConfig(Backend)(
			Classpaths.ivyBaseSettings ++
			Classpaths.jvmBaseSettings ++
			Defaults.compileBase ++
			Defaults.configTasks ++
			Defaults.configSettings ++
			Revolver.settings ++
			Seq(
				mainClass in Revolver.reStart := Some("com.scalakata.backend.Boot"),
				fullClasspath in Revolver.reStart <<= fullClasspath,
				Revolver.reStart <<= InputTask(Actions.startArgsParser) { args ⇒
					(
						streams,
						Revolver.reLogTag,
						thisProjectRef,
						Revolver.reForkOptions,
						mainClass in Revolver.reStart,
						fullClasspath in Revolver.reStart,
						startArgs in Revolver.reStart,
						args
					).map(Actions.restartApp)
					 .dependsOn(products in Compile)
				},
				kataUri := new URI("http://localhost:7331"),
				readyPort := 8081,
				openBrowser := {
					val socket = new java.net.ServerSocket(readyPort.value)
					socket.accept()
					socket.close()

					sys.props("os.name").toLowerCase match {
	          case x if x contains "mac" => s"open ${kataUri.value.toString}".!
	          case _ => Desktop.getDesktop.browse(kataUri.value)
	        }

					()
				},
				libraryDependencies ++= Seq(
					"com.scalakata" % s"backend_${scalaBinaryVersion.value}" % scalaKataVersion,
					"com.scalakata" % s"eval_${scalaBinaryVersion.value}" % scalaKataVersion,
					"com.scalakata" % "frontend" % scalaKataVersion
				)
			)
		) ++
		inConfig(Kata)(
			Classpaths.ivyBaseSettings ++
			Classpaths.jvmBaseSettings ++
			Defaults.configSettings ++
			Defaults.compileBase ++
			Seq(
				scalaVersion := "2.11.2",
				scalacOptions += "-Yrangepos",
				libraryDependencies ++= Seq(
					"com.scalakata" % s"macro_${scalaBinaryVersion.value}" % scalaKataVersion,
					"org.scala-lang" % "scala-compiler" % scalaVersion.value,
					compilerPlugin("org.scalamacros" % s"paradise_${scalaVersion.value}" % "2.1.0-M1")
				),
				initialCode := ("", "")
			)
		) ++
		Seq(
			// the backend can serve .scala files
			unmanagedResourceDirectories in Backend <+= sourceDirectory in Kata,
			scalaVersion in Backend <<= scalaVersion in Kata,
			startArgs in (Backend, Revolver.reStart) := Seq(
				(readyPort in Backend).value.toString,
				((fullClasspath in Compile).value ++ (dependencyClasspath in Kata).value).
					map(_.data).
					map(_.getAbsoluteFile).
					mkString(File.pathSeparator),
				(kataUri in Backend).value.getHost,
				(kataUri in Backend).value.getPort.toString,
				(initialCode in Kata).value._1,
				(initialCode in Kata).value._2
			) ++ (scalacOptions in Kata).value,
			resolvers ++= Seq(
				"spray repo" at "http://repo.spray.io",
				"typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
				"masseguillaume" at "http://dl.bintray.com/content/masseguillaume/maven",
				Resolver.sonatypeRepo("releases")
			)
		)
}
