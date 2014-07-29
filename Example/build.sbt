scalaVersion := "2.11.2"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6"

seq(kataSettings: _*)

initialCommands in Kata :=
"""|import scalaz._
   |import std.option._, std.list._
   |
   |Apply[Option].apply2(some(1), some(2))((a, b) => a + b)
   |
   |Traverse[List].traverse(List(1, 2, 3))(i => some(i))""".stripMargin
