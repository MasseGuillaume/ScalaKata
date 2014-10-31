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

import sbtdocker._
import sbtdocker.Plugin._
import sbtdocker.Plugin.DockerKeys._

object Scalakata extends Plugin {

	case class StartArgs(
		readyPort: Int,
		classPath: Seq[File],
		host: String,
		port: Int,
		production: Boolean,
		security: Boolean,
		scalacOptions: Seq[String]
	) {
		def toArgs = Seq(
			readyPort.toString,
			classPath.
				map(_.getAbsoluteFile).
				mkString(File.pathSeparator),
			host,
			port.toString,
			production.toString,
			security.toString
		) ++ scalacOptions
	}

	lazy val Kata = config("kata") extend(Runtime)
	lazy val Backend = config("backend")

	lazy val openBrowser = TaskKey[Unit]("open-browser", "task to open browser to kata url")
	lazy val readyPort = SettingKey[Int]("ready-port", "port to send ready command")
	lazy val kataUri = SettingKey[URI]("kata-uri", "uri to scala kata")
	lazy val startArgs = TaskKey[StartArgs]("start-args",
    	"The arguments to be passed to the applications main method when being started")
	lazy val startArgs2 = TaskKey[Seq[String]]("start-args2",
			"The arguments to be passed to the applications main method when being started")

  lazy val securityManager = SettingKey[Boolean]("security-manager", "turn on jvm security manager")
	lazy val production = SettingKey[Boolean]("production", "deployed version")

	lazy val test = Project(
		id = "test",
		base = file("."),
		settings = kataSettings
	)
	lazy val scalaKataVersion = "0.9.0"
	val start = "kstart"

	lazy val kataAutoStart =
		onLoad in Global := {
			((s: State) ⇒ { start :: s }) compose (onLoad in Global).value
		}

	lazy val kataSettings =
		Project.defaultSettings ++
		addCommandAlias(start, ";backend:reStart ;backend:openBrowser ;kwatch") ++
		addCommandAlias("kwatch", "~ ;backend:copyResources ;kata:compile ;kata:copyResources") ++
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
				production := false,
				securityManager := false,
				mainClass in Revolver.reStart := Some("com.scalakata.backend.Boot"),
				startArgs2 in Revolver.reStart := (startArgs in Revolver.reStart).value.toArgs,
				fullClasspath in Revolver.reStart <<= fullClasspath,
				Revolver.reStart <<= InputTask(Actions.startArgsParser) { args ⇒
					(
						streams,
						Revolver.reLogTag,
						thisProjectRef,
						Revolver.reForkOptions,
						mainClass in Revolver.reStart,
						fullClasspath in Revolver.reStart,
						startArgs2 in Revolver.reStart,
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
	          case x if x contains "mac" ⇒ s"open ${kataUri.value.toString}".!
	          case _ ⇒ Desktop.getDesktop.browse(kataUri.value)
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
			Defaults.compileBase ++
			Defaults.configTasks ++
			Defaults.configSettings ++
			Seq(
				scalaVersion := "2.11.2",
				unmanagedResourceDirectories += sourceDirectory.value,
				scalacOptions ++= Seq("-Yrangepos", "-unchecked", "-deprecation", "-feature"),
				libraryDependencies ++= Seq(
					"com.scalakata" % s"macro_${scalaBinaryVersion.value}" % scalaKataVersion,
					"org.scala-lang" % "scala-compiler" % scalaVersion.value,
					compilerPlugin("org.scalamacros" % s"paradise_${scalaVersion.value}" % "2.1.0-M1")
				)
			)
		) ++
		Seq(
			// the backend can serve .scala files
			unmanagedResourceDirectories in Backend += (sourceDirectory in Kata).value,
			// you have full acces to your project in the kata sandbox
			dependencyClasspath in Kata ++= (fullClasspath in Compile).value,
			scalaVersion in Backend := (scalaVersion in Kata).value,
			startArgs in (Backend, Revolver.reStart) := StartArgs(
				(readyPort in Backend).value,
				(fullClasspath in Kata).value.
					map(_.data).
					map(_.getAbsoluteFile),
				(kataUri in Backend).value.getHost,
				(kataUri in Backend).value.getPort,
				(production in Backend).value,
				(securityManager in Backend).value,
				(scalacOptions in Kata).value
			),
			resolvers ++= Seq(
				"spray repo" at "http://repo.spray.io",
				"typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
				"masseguillaume" at "http://dl.bintray.com/content/masseguillaume/maven",
				Resolver.sonatypeRepo("releases")
			)
		)

	lazy val kataDockerSettings = kataSettings ++ inConfig(Backend)(Seq(
      production := true,
      securityManager := true,
      openBrowser := { }
    )) ++ inConfig(Kata)(dockerSettings) ++ Seq(
			kataUri in Backend := new URI("http://0.0.0.0:7331"),
      imageName in (Kata, docker) := {
        ImageName(
          namespace = None,
          repository = name.value,
          tag = Some("v" + version.value)
        )
      },
      dockerfile in (Kata, docker) := {
        val Some(main) = (mainClass in (Backend, Revolver.reStart)).value

        val app = "/app"
        val libs = s"$app/libs"
        val katas = s"$app/katas"
        val plugins = s"$app/plugins"

        val classpath = s"$libs/*:$katas/*"

        new Dockerfile {
					from("dockerfile/java:oracle-java8")

          val args = {
            val t = (startArgs in (Backend, Revolver.reStart)).value
            val kataClasspath =
							(packageBin in Compile).value +:
							(packageBin in Kata).value +:
							(managedClasspath in Kata).value.
                 map(_.data).
                 map(_.getAbsoluteFile)
            t.copy(
              // update compiler plugin path
              scalacOptions = t.scalacOptions.map{ v ⇒
                val pluginArg = "-Xplugin:"
                if(v.startsWith(pluginArg)) {
                  val plugin = file(v.slice(pluginArg.length, v.length))
                  val target = file(plugins) / plugin.name
                  stageFile(plugin,  target)
                  pluginArg + target.getAbsolutePath
                } else v
              },
              // update frontend classpath
              classPath = kataClasspath.map { v ⇒
                val target = file(katas) / v.name
                stageFile(v, target)
                target
              }
            )
          }

          // backend classpath
          (managedClasspath in Backend).value.files.foreach{ dep ⇒
            val target = file(libs) / dep.name
            stageFile(dep, target)
          }
          add(libs, libs)

          // frontend classpath
          add(katas, katas)
          add(plugins, plugins)

          // exposes
          expose(args.port)
          entryPoint((
						Seq("java", "-Xmx1G", "-Xms256M", "-cp", classpath, main) ++ args.toArgs
					):_*)
        }
      }
    )
}
