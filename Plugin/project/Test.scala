import sbt._
import Keys._

object EvalBuild extends Build {

	val kata = config("kata")

	lazy val kstart = taskKey[Unit]("Start scala kata")

	lazy val test = Project(
		id = "test",
		base = file("."),
		settings = Project.defaultSettings ++ Seq(
			offline := true,
			fork in kstart := true,
			connectInput in kstart := true,
			ivyConfigurations += kata,
			scalaVersion in kata := "2.11.2-SNAPSHOT",
			libraryDependencies ++= Seq(
				"org.scalamacros" % s"paradise_${(scalaVersion in kata).value}" % "2.1.0-SNAPSHOT" % "kata",
				"org.scala-lang" % "scala-compiler" % (scalaVersion in kata).value % "kata"
				//"org.scala-lang" % "scala-reflect" % (scalaVersion in kata).value ???
			),
			scalaSource in kata := sourceDirectory.value / "kata",
			dependencyClasspath in kata := update.value.select(configurationFilter("kata")) map(Attributed.blank),
			kstart := {
				start()
			}
		)
	)
	def start(): Unit = {
		()
	}
}