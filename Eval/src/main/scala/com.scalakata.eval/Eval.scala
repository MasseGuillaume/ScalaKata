package com.scalakata.eval

// based on twitter util eval
// https://github.com/twitter/util/tree/master/util-eval

import java.io._
import java.math.BigInteger
import java.net.URLClassLoader
import java.security.MessageDigest
import java.util.Random

import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.io.{AbstractFile, VirtualDirectory}
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.util.{BatchSourceFile, Position}

class Eval {

  def apply[T](code: String): Either[Map[String, List[(Int, String)]], T] = {
    val id = uniqueId(code)
    val className = "Evaluator__" + id
    compile(wrapCodeInClass(className, code), className)
    
    val infos = check(className)

    if(!infos.contains(reporter.ERROR)) {
      val cls = classLoader.loadClass(className)
      val t: T = cls.getConstructor().newInstance().asInstanceOf[() => Any].apply().asInstanceOf[T]
      Right(t)
    } else {
      Left(infos.map{case (k, v) => (k.toString, v)})
    }
    
  }

  val jvmId = java.lang.Math.abs(new Random().nextInt())
 
  private def uniqueId(code: String, idOpt: Option[Int] = Some(jvmId)): String = {
    val digest = MessageDigest.getInstance("SHA-1").digest(code.getBytes())
    val sha = new BigInteger(1, digest).toString(16)
    idOpt match {
      case Some(id) => sha + "_" + jvmId
      case _ => sha
    }
  }

  private def check(className: String): Map[reporter.Severity, List[(Int, String)]] = {
    reporter.infos.map {
      info => (
        info.severity,
        info.pos.point - preWrap(className).length,
        info.msg
      )
    }.to[List]
     .filterNot{ case (sev, _, msg) =>
      // annoying
      sev == reporter.WARNING &&
      msg == ("a pure expression does nothing in statement " +
              "position; you may be omitting necessary parentheses")
    }.groupBy(_._1)
     .mapValues{_.map{case (a,b,c) => (b,c)}}
  }

  def preWrap(className: String) =
    "class " + className + " extends (() => Any) {\n" +
    "  def apply() = {\n"
  private def wrapCodeInClass(className: String, code: String) = {
    preWrap(className) +
    code + "\n" +
    "  }\n" +
    "}\n"
  }

  
  val target = new VirtualDirectory("(memory)", None)

  val settings = new Settings
  settings.outputDirs.setSingleOutput(target)

  val artifacts = sbt.BuildInfo.dependencyClasspath.
    map(_.getAbsoluteFile).
    mkString(File.pathSeparator)

  settings.bootclasspath.value = artifacts
  settings.classpath.value = artifacts

  val reporter = new StoreReporter()

  val global = new Global(settings, reporter)

  var classLoader = new AbstractFileClassLoader(target, this.getClass.getClassLoader)

  private def reset() {
    target.clear
    reporter.reset
    classLoader = new AbstractFileClassLoader(target, this.getClass.getClassLoader)
  }

  private def compile(code: String, className: String): Unit = {
    synchronized {
      reset()
      val compiler = new global.Run
      val sourceFiles = List(new BatchSourceFile("(inline)", code))

      compiler.compileSources(sourceFiles)
    }
  }
}