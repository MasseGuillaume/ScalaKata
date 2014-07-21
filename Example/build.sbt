scalaVersion := "2.11.2-SNAPSHOT"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6"

seq(kataSettings: _*)

initialCommands in Kata := 
"""
// Initial commands is
List(1 ,2)
"""