import sbt._
import sbt.Keys._

object Settings {
  lazy val default = Project.defaultSettings ++ Seq(
    organization := "com.scalakata",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.1"
  )

  import com.earldouglas.xsbtwebplugin._
  import WebPlugin._
  import PluginKeys._
  import WebappPlugin._
  lazy val web = default ++ webSettings ++ Seq(
    scanDirectories in Compile := Nil,
    
    port in container.Configuration := 8080,
    classesAsJar in Compile := true
  )
}