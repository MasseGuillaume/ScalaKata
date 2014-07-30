package com.scalakata.eval

import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.io.{AbstractFile, VirtualDirectory}
import scala.tools.nsc.util.{BatchSourceFile, Position}
import scala.tools.nsc.reporters.StoreReporter

import java.net.{URL, URLClassLoader}
import java.io.File

object Eval {
  type Instrumentation = scala.collection.mutable.Map[(Int, Int), Any]
}

class Eval(settings: Settings) {
  private val reporter = new StoreReporter()

  private val artifactLoader = {
    val loaderFiles =
      settings.classpath.value.split(File.pathSeparator).map(a ⇒ {
        new URL(s"file://$a/")
      })
    new URLClassLoader(loaderFiles, this.getClass.getClassLoader)
  }

  private val target = new VirtualDirectory("(memory)", None)
  private var classLoader: AbstractFileClassLoader = _

  settings.outputDirs.setSingleOutput(target)
  settings.Ymacroexpand.value = settings.MacroExpand.Normal

  private val compiler = new Global(settings, reporter)
  private val objectName = "A"

  def apply(code: String): (Option[Eval.Instrumentation], Map[String, List[(Int, String)]]) = {
    compile(code)

    val infos = check()
    val infoss = infos.map{case (k, v) ⇒ (k.toString, v)}

    if(!infos.contains(reporter.ERROR)) {
      import scala.reflect.runtime.{universe ⇒ ru}
      val m = ru.runtimeMirror(classLoader)
      val lm = m.staticModule(objectName)
      val obj = m.reflectModule(lm)
      val instr = obj.instance.asInstanceOf[{def eval$(): Eval.Instrumentation}].eval$()
      (Some(instr), infoss)
    } else {
      (None, infoss)
    }
  }

  private def check(): Map[reporter.Severity, List[(Int, String)]] = {
    reporter.infos.map {
      info ⇒ (
        info.severity,
        info.pos.point,
        info.msg
      )
    }.to[List]
     .filterNot{ case (sev, _, msg) ⇒
      // annoying
      sev == reporter.WARNING &&
      msg == ("a pure expression does nothing in statement " +
              "position; you may be omitting necessary parentheses")
    }.groupBy(_._1)
     .mapValues{_.map{case (a,b,c) ⇒ (b,c)}}
  }

  private def reset(): Unit = {
    target.clear
    reporter.reset
    classLoader = new AbstractFileClassLoader(target, artifactLoader)
  }

  private def compile(code: String): Unit = {
    reset()
    val run = new compiler.Run
    val sourceFiles = List(new BatchSourceFile("(inline)", code))

    run.compileSources(sourceFiles)
  }
}
