name := "frontend"

organization := "com.scalakata"

versionWithGit

git.baseVersion := "0.1"

scalaVersion := "2.11.1" // whatever

resourceGenerators in Compile += Def.task {
  //"gulp build" ! streams.value.log
  val base = baseDirectory.value
  (base / "dist" ***).get
}.taskValue

mappings in (packageBin in Compile) := {
  val base = baseDirectory.value
  val files = (base / "dist" ***).get
  files pair relativeTo(base)
}

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false