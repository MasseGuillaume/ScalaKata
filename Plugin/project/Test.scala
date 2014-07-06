import sbt._
import Keys._

object EvalBuild extends Build {

	lazy val Kata = config("kata") extend (Compile)

	lazy val kstart = taskKey[Unit]("Start scala kata")

	lazy val test = Project(
		id = "test",
		base = file("."),
		settings = Project.defaultSettings ++ 
		inConfig(Kata)(Defaults.configSettings) ++ Seq(
			offline := true,
			fork in kstart := true,
			connectInput in kstart := true,
			ivyConfigurations += Kata,
			scalaVersion in Kata := "2.11.2-SNAPSHOT",
			libraryDependencies ++= Seq(
				"org.scalamacros" % s"paradise_${(scalaVersion in Kata).value}" % "2.1.0-SNAPSHOT" % "kata",
				"org.scala-lang" % "scala-compiler" % (scalaVersion in Kata).value % "kata"
				//"org.scala-lang" % "scala-reflect" % (scalaVersion in Kata).value ???
			),
			scalaSource in Kata := sourceDirectory.value / "kata",
			dependencyClasspath in Kata := update.value.select(configurationFilter("kata")) map(Attributed.blank),
			kstart := {
				start()
			}
		)
	)
	def start(): Unit = {
		()
	}
}