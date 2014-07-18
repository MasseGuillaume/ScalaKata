scalaVersion := "2.11.2-SNAPSHOT"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6"

libraryDependencies in Backend := Seq(
	"com.scalakata" %% "backend"  % "0.3.0-SNAPSHOT",
	"com.scalakata" %% "eval"     % "0.3.0-SNAPSHOT"
)

libraryDependencies in Kata := Seq(
	"com.scalakata" %% "macro" % "0.3.0-SNAPSHOT",
	"org.scala-lang" % "scala-compiler" % scalaVersion.value,
	compilerPlugin("org.scalamacros" % s"paradise_${scalaVersion.value}" % "2.1.0-SNAPSHOT")
)

resolvers += Resolver.sonatypeRepo("snapshots")

seq(kataSettings: _*)