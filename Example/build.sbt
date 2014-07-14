scalaVersion := "2.11.1"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6"

resolvers ++= Seq(
	"spray repo" at "http://repo.spray.io",
	"typesafe releases" at "http://repo.typesafe.com/typesafe/releases"
)

seq(kataSettings: _*)
