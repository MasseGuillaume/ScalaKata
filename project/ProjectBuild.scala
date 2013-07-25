import sbt._
import sbt.Keys._

object ProjectBuild extends Build {
  import Dependencies._
  lazy val web = Project(
    id = "Web",
    base = file("."),
    settings = Settings.web ++ Seq(
      name := "scala-kata",
      libraryDependencies ++= webStack ++ frontendDependencies ++ Seq(specs2)
    )
  )
}