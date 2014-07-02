name := "frontend"

organization := "com.scalakata"

versionWithGit

git.baseVersion := "0.1"

scalaVersion := "2.11.1" // whatever

resourceDirectory in Compile := {
	baseDirectory.value / "dist"
}

resourceGenerators in Compile += Def.task {
  "gulp build" ! streams.value.log
  (baseDirectory.value / "dist" ***).get
}.taskValue

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false