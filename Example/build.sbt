scalaVersion := "2.11.2"

seq(kataSettings: _*)

kataAutoStart

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
