import sbt._
import sbt.Keys._

import com.untyped.sbtjs.Plugin._
import com.untyped.sbtless.Plugin._

object Settings {
  lazy val default = Project.defaultSettings ++ Seq(
    organization := "com.scalakata",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.1"
  )

  import com.earldouglas.xsbtwebplugin._
  import WebPlugin._
  import PluginKeys._
  lazy val web = default ++ webSettings ++ jsSettings ++ lessSettings ++ Seq(
    scanDirectories in Compile := Nil,
    port in container.Configuration := 8080,
    (webappResources in Compile) <+= (resourceManaged in Compile),
    (compile in Compile) <<= compile in Compile dependsOn (LessKeys.less in Compile),
    (compile in Compile) <<= compile in Compile dependsOn (JsKeys.js in Compile)
  )
}
