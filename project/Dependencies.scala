import sbt._
import sbt.Keys._

object Dependencies {
  private lazy val liftVersion = "2.5"
  lazy val webStack = Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile",
    "net.liftweb" %% "lift-json" % "2.0" % "compile",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime,compile"
  )

 lazy val specs2 = "org.specs2" %% "specs2" % "2.2-SNAPSHOT"

  lazy val frontendDependencies = Seq(
    "org.webjars" % "codemirror" % "3.11" % "runtime",
    "org.webjars" % "jquery" % "2.0.0" % "runtime",
    "org.webjars" % "bootstrap" % "2.3.1-1" % "runtime",
    "org.webjars" % "webjars-locator" % "0.5" % "compile"
  )
}