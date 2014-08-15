package com.scalakata.eval

import scala.tools.nsc.{Global, Settings}
import scala.reflect.internal.util.{BatchSourceFile, AbstractFileClassLoader}

import scala.tools.nsc.io.VirtualDirectory

import scala.tools.nsc.reporters.StoreReporter

import java.net.{URL, URLClassLoader}
import java.io.File

object Eval {
  type Instrumentation = scala.collection.mutable.Map[(Int, Int), (String, RenderType)]
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

  def apply(code: String): (Option[Eval.Instrumentation], Map[String, List[(Int, String)]]) = {
    compile(code)

    val infos = check()
    val infoss = infos.map{case (k, v) ⇒ (k.toString, v)}

    if(!infos.contains(reporter.ERROR)) {
      // Look for static class with eval$ method that return
      // an instrumentation
      def findEval: Option[Eval.Instrumentation] = {
        def removeExt(of: String) = {
        	of.slice(0, of.lastIndexOf(".class"))
        }

        val classes = target.iterator.
          map(v => removeExt(v.name)).
          filterNot(_.contains("$")).
          map(classLoader.findClass).toList

        val instrClass =
        	classes.find(_.getMethods.exists(m =>
            m.getName == "eval$" &&
        		m.getReturnType == classOf[scala.collection.mutable.Map[_, _]])
        	)

        import scala.reflect.runtime.{universe => ru}
        val m = ru.runtimeMirror(classLoader)

        instrClass.map{c =>
          m.reflectModule(m.staticModule(c.getName)).
            instance.asInstanceOf[{def eval$(): Eval.Instrumentation}].eval$()
        }
      }

      (findEval, infoss)
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
