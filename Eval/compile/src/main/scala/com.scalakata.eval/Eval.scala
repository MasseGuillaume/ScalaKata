package com.scalakata.eval

import scala.tools.nsc.{Global, Settings}
import scala.reflect.internal.util.{BatchSourceFile, AbstractFileClassLoader}

import scala.tools.nsc.io.{VirtualDirectory, AbstractFile}
import scala.reflect.internal.util.NoPosition

import scala.tools.nsc.reporters.StoreReporter

import java.net.{URL, URLClassLoader}
import java.io.File

import scala.language.reflectiveCalls

class Eval(settings: Settings, security: Boolean) {

  if(security) {
    Security.start
  }

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

  def apply(code: String): EvalResponse = {
    compile(code)

    val infos = check()

    if(!infos.contains(Error)) {
      // Look for static class with eval$ method that return
      // an instrumentation
      def findEval: Option[OrderedRender] = {
        def removeExt(of: String) = {
        	of.slice(0, of.lastIndexOf(".class"))
        }

        def removeMem(of: String) = {
          of.slice("(memory)/".length, of.length)
        }

        def recurseFolders(file: AbstractFile): Set[AbstractFile] = {
          file.iterator.to[Set].flatMap{ fs =>
            if(fs.isDirectory)
              fs.to[Set] ++
              fs.filter(_.isDirectory).flatMap(recurseFolders).to[Set]
            else Set(fs)
          }
        }

        val instrClass =
          recurseFolders(target).
          map(_.path).
          map(((removeExt _) compose (removeMem _))).
          map(_.replace('/', '.')).
          filterNot(c => c.endsWith("$") || c.endsWith("$class")).
          find { n =>
            classLoader.loadClass(n).getMethods.exists(m =>
              m.getName == "eval$" &&
              m.getReturnType == classOf[OrderedRender]
            )
          }

        import scala.reflect.runtime.{universe => ru}
        val m = ru.runtimeMirror(classLoader)

        instrClass.map{c =>
          m.reflectModule(m.staticModule(c)).
            instance.asInstanceOf[{def eval$(): OrderedRender}].eval$()
        }
      }

      EvalResponse.empty.copy(
        insight = findEval.getOrElse(Nil),
        infos = infos
      )
    } else {
      EvalResponse.empty.copy(infos = infos)
    }
  }

  private def check(): Map[Severity, List[CompilationInfo]] = {
    val infos =
      reporter.infos.map { info ⇒
        val (start, end) = info.pos match {
          case NoPosition => (0, 0)
          case _ => (info.pos.start, info.pos.end)
        }
        (
          info.severity,
          start,
          end,
          info.msg
        )
      }.to[List]
       .filterNot{ case (sev, _, _, msg) ⇒
        // annoying
        sev == reporter.WARNING &&
        msg == ("a pure expression does nothing in statement " +
                "position; you may be omitting necessary parentheses")
      }.groupBy(_._1)
       .mapValues{_.map{case (_,start, end, message) ⇒ (start, end, message)}}

    def convert(infos: Map[reporter.Severity, List[(Int, Int, String)]]): Map[Severity, List[CompilationInfo]] = {
      infos.map{ case (k,vs) ⇒
        val sev = k match {
          case reporter.ERROR ⇒ Error
          case reporter.WARNING ⇒ Warning
          case reporter.INFO ⇒ Info
        }
        val info = vs map {case (start, end, message) ⇒
          CompilationInfo(message, start, end)
        }
        (sev, info)
      }
    }
    convert(infos)
  }

  private def reset(): Unit = {
    target.clear()
    reporter.reset()
    classLoader = new AbstractFileClassLoader(target, artifactLoader)
  }

  private def compile(code: String): Unit = {
    reset()
    val run = new compiler.Run
    val sourceFiles = List(new BatchSourceFile("(inline)", code))

    run.compileSources(sourceFiles)
  }
}
