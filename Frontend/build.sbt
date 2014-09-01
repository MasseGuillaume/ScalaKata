name := "frontend"

organization := "com.scalakata"

version := "0.6.1-SNAPSHOT"

autoScalaLibrary := false

scalaVersion := "2.11.2"

resourceDirectory in Compile := {
	baseDirectory.value / "out"
}

resourceGenerators in Compile += Def.task {
  "gulp build" ! streams.value.log
  ((resourceDirectory in Compile).value ***).get
	/*Seq.empty[java.io.File]*/
}.taskValue

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

crossPaths := false

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(bintrayPublishSettings:_*)
