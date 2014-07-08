import sbt._
import Def.Initialize
import Keys._
import Attributed.data
import spray.revolver.RevolverPlugin.Revolver

object EvalBuild extends Build {

	private lazy val Kata = config("kata")
	private lazy val Backend = config("backend")
	
	val openBrowser = TaskKey[Unit]("open-browser", "task to open browser to kata url")
  	val kataUrl = SettingKey[String]("kata-url", "url to scala kata")

	// private lazy val kstart = taskKey[Unit]("start scala kata")
	// private lazy val kstop = taskKey[Unit]("stop scala kata")

	lazy val hack = Project(
		id = "hack",
		base = file("hack"),
		settings = Project.defaultSettings ++ Seq(
			offline := true,
			scalaVersion := "2.11.2-SNAPSHOT"
		)
	)

	lazy val test = Project(
		id = "test",
		base = file("."),
		settings = 
		Project.defaultSettings ++ 
		inConfig(Backend)(
			Defaults.configSettings ++
			Revolver.settings
		) ++
		inConfig(Kata)(Defaults.configSettings) ++ 
		addCommandAlias("kstart", ";backend:reStart ;openBrowser") ++
		addCommandAlias("kstop", "backend:reStop") ++
		Seq(
			openBrowser := ""
			offline := true,
			ivyConfigurations ++= Seq(Kata, Backend),
			mainClass in (Backend, Revolver.reStart) := Some("com.scalakata.backend.Boot"),
			fullClasspath in (Backend, Revolver.reStart) <<= fullClasspath in Backend,
			scalaVersion in Kata := "2.11.2-SNAPSHOT",
			scalaVersion in Backend := (scalaVersion in Kata).value,
			scalaInstance in Backend := (scalaInstance in v).value,
			libraryDependencies ++= Seq(
				"org.scalamacros" % s"paradise_${(scalaVersion in Kata).value}" % "2.1.0-SNAPSHOT" % "kata",
				"org.scala-lang" % "scala-compiler" % (scalaVersion in Kata).value % "kata",
				//"org.scala-lang" % "scala-reflect" % (scalaVersion in Kata).value ???
				"com.scalakata" % s"backend_${(scalaBinaryVersion in Backend).value}" % "0.1-20140706T182100" % "backend"
			),
			scalaSource in Kata := sourceDirectory.value / "kata"
		)
	)
}