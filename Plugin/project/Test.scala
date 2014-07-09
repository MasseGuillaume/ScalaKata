import sbt._
import Def.Initialize
import Keys._
import Attributed.data

import java.net.URL
import java.io.File

import spray.revolver.Actions
import spray.revolver.RevolverPlugin.Revolver

object EvalBuild extends Build {

	private lazy val Kata = config("kata")
	private lazy val Backend = config("backend")
	
	val openBrowser = TaskKey[Unit]("open-browser", "task to open browser to kata url")
	val kataUrl = SettingKey[URL]("kata-url", "url to scala kata")
	val startArgs = TaskKey[Seq[String]]("start-args",
    	"The arguments to be passed to the applications main method when being started")

	lazy val test = Project(
		id = "test",
		base = file("."),
		settings = 
		Project.defaultSettings ++
		inConfig(Backend)(
			Classpaths.ivyBaseSettings ++
			Classpaths.jvmBaseSettings ++ 
			Defaults.compileBase ++ 
			Defaults.configSettings ++
			Revolver.settings
		) ++
		inConfig(Kata)(
			Classpaths.ivyBaseSettings ++
			Classpaths.jvmBaseSettings ++
			Defaults.configSettings ++
			Defaults.compileBase
		) ++ 
		addCommandAlias("kstart", ";backend:reStart ;openBrowser") ++
		addCommandAlias("kstop", "backend:reStop") ++
		Seq(
			resolvers ++= Seq(
				Resolver.sonatypeRepo("releases"),
				Resolver.sonatypeRepo("snapshots")
			),
			mainClass in (Backend, Revolver.reStart) := Some("com.scalakata.backend.Boot"),
			fullClasspath in (Backend, Revolver.reStart) <<= fullClasspath in Backend,
			Revolver.reStart in Backend <<= InputTask(Actions.startArgsParser) { args =>
				(
					streams, 
					Revolver.reLogTag in Backend,
					thisProjectRef, 
					Revolver.reForkOptions in Backend, 
					mainClass in (Backend, Revolver.reStart), 
					fullClasspath in (Backend, Revolver.reStart), 
					startArgs in (Backend, Revolver.reStart), 
					args
				).map(Actions.restartApp)
				 .dependsOn(products in Compile)
			},
			startArgs in (Backend, Revolver.reStart) := Seq(
				(fullClasspath in Kata).value.
					map(_.data).
					map(_.getAbsoluteFile).
    				mkString(File.pathSeparator),
    			kataUrl.value.getHost,
    			kataUrl.value.getPort.toString
			),
			scalaVersion in Kata := "2.11.2-SNAPSHOT",
			scalaVersion in Backend := (scalaVersion in Kata).value,
			libraryDependencies in Kata ++= Seq(
				"org.scalamacros" % s"paradise_${(scalaVersion in Kata).value}" % "2.1.0-SNAPSHOT",
				"org.scala-lang" % "scala-compiler" % (scalaVersion in Kata).value
				//"org.scala-lang" % "scala-reflect" % (scalaVersion in Kata).value ???
			),
			libraryDependencies in Backend ++= Seq(
				"com.scalakata" % s"backend_${(scalaBinaryVersion in Backend).value}" % "0.1-20140706T182100"
			),
			scalaSource in Kata := sourceDirectory.value / "kata",
			//dependencyClasspath in Kata := update.value.select(configurationFilter("kata")) map(Attributed.blank),
			kataUrl := new URL("http://localhost:8080"),
			openBrowser := { 
				Thread.sleep(500)
				s"google-chrome ${kataUrl.value.toString}"! 
			}
		)
	)
}