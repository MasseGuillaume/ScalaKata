name := "frontend"

offline := true

organization := "com.scalakata"

versionWithGit

git.baseVersion := "0.1"

autoScalaLibrary := false

scalaVersion := "2.11.1"

resourceDirectory in Compile := {
	baseDirectory.value / "dist"
}

resourceGenerators in Compile += Def.task {
  "gulp build" ! streams.value.log
  (baseDirectory.value / "dist" ***).get
}.taskValue

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html"))

seq(bintrayPublishSettings:_*)