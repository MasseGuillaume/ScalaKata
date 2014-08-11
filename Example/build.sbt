scalaVersion := "2.11.2"

seq(kataSettings: _*)

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

initialCode in Kata := ("","")
