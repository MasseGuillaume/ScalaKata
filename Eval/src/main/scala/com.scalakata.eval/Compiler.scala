package com.scalakata.eval

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Random
import java.util.concurrent.{TimeoutException, Callable, FutureTask, TimeUnit}

import scala.util.control.NonFatal
import scala.concurrent.duration._

class Compiler {

  val timeout = 60.seconds
  val jvmId = java.lang.Math.abs(new Random().nextInt())

  import scala.tools.nsc.interactive.Global
  import scala.tools.nsc.Settings
  import scala.tools.nsc.reporters.StoreReporter
  import scala.tools.nsc.io.VirtualDirectory
  import scala.reflect.internal.util._
  import scala.tools.nsc.interactive.Response

  val reporter = new StoreReporter()
  val settings = new Settings()
  
  val artifacts = sbt.BuildInfo.dependencyClasspath.
    map(_.getAbsoluteFile).
    mkString(File.pathSeparator)

  val target = new VirtualDirectory("(memory)", None)
  var classLoader = new AbstractFileClassLoader(target, this.getClass.getClassLoader)

  settings.outputDirs.setSingleOutput(new VirtualDirectory("(memory)", None))
  settings.bootclasspath.value = artifacts
  settings.classpath.value = artifacts
  settings.outputDirs.setSingleOutput(target)

  val compiler = new Global(settings, reporter)

  def insight(code: String): EvalResponse = {
    if (code == "") EvalResponse.empty
    else {
      try { 
        withTimeout{ eval(code) }(timeout) match {
          case Some((insight, compilationInfos)) => EvalResponse(
            insight, 
            compilationInfos,
            timeout = false,
            runtimeError = None
          )
          case None => EvalResponse(
            insight = Nil,
            infos = Nil,
            timeout = true,
            runtimeError = None
          )
        }
      } catch {
        case NonFatal(e) => {
          e.printStackTrace

          val error = 
            for {
              e1 <- Option(e.getCause)
              e2 <- Option(e1.getCause)
            } yield {
              RuntimeError(e2.toString, e2.getStackTrace.head.getLineNumber)
            }
          EvalResponse(
            insight = Nil,
            infos = Nil,
            timeout = false,
            runtimeError = error
          )
        }
      }
    }
  }

  def autocomplete(code: String, pos: Int): List[CompletionResponse] = {
    if(code.isEmpty) Nil
    else {
      val file = reload(code)
      val ajustedPos = pos + wrapOffset
      val position = new OffsetPosition(file, ajustedPos)
      val response = withResponse[List[compiler.Member]](r => 
        compiler.askTypeCompletion(position, r)
      )

      response.get match {
        case Left(members) => compiler.ask( () => {
          members.map(member => 
            CompletionResponse(
              name = member.sym.decodedName,
              signature = member.sym.signatureString
            )
          )
        })
        case Right(e) => 
          e.printStackTrace
          Nil
      }
    }
  }

  private def eval[T](code: String): (T, List[CompilationInfo])  = {
    def uniqueId: String = {
      val digest = MessageDigest.getInstance("SHA-1").digest(code.getBytes())
      val sha = new BigInteger(1, digest).toString(16)
      s"${sha}_${jvmId}"
    }

    synchronized {
      def reset(): Unit = {
        target.clear()
        reporter.reset()
        classLoader = new AbstractFileClassLoader(target, this.getClass.getClassLoader)
      }
      reset()
      val className = s"ScalaKata_$uniqueId"

      val run = new compiler.Run
      val sourceFiles = List(new BatchSourceFile("(inline)", code))
      compiler.ask { () =>
        run.compileSources(sourceFiles)
      }
      val infos = check

      val cls = classLoader.loadClass(className)
      val res = cls.getConstructor().newInstance().asInstanceOf[() => Any].apply().asInstanceOf[T]

      (res, infos)
    }
  }

  private def withTimeout[T](f: => T)(timeout: Duration): Option[T]= {
    val task = new FutureTask(new Callable[T]() {
      def call = f
    })
    val thread = new Thread( task )
    try {
      thread.start()
      Some(task.get(timeout.toMillis, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException => None
    } finally { 
      if( thread.isAlive ){
        thread.interrupt()
        thread.stop()
      }
    }
  }

  private val beginWrap = "object ScalaKata {\n"
  private val endWrap = "\n}"

  private val wrapOffset = beginWrap.size

  private def check: List[CompilationInfo] = {
    //parse(code)
    def annoying(info: CompilationInfo) = {
      info.message == "a pure expression does nothing in statement " +
        "position; you may be omitting necessary parentheses" &&
      info.severity == Warning
    }
    reporter.infos.map {
      info => CompilationInfo(
        message = info.msg,
        position = info.pos.point - wrapOffset,
        severity = convert(info.severity) 
      )
    }.filterNot(annoying).to[List]
  }

  private def wrap(code: String): BatchSourceFile = {
    new BatchSourceFile("default", beginWrap + code + endWrap)
  }

  private def reload(code: String): BatchSourceFile = {
    val file = wrap(code)
    withResponse[Unit](r => compiler.askReload(List(file), r)).get
    file
  }

  // private def parse(code: String): Unit = {
  //   val file = reload(code)
  //   withResponse[compiler.Tree](r => compiler.askStructure(false)(file, r)).get
  // }

  private def convert(severity: reporter.Severity): Severity = severity match {
    case reporter.INFO => Info
    case reporter.WARNING => Warning
    case reporter.ERROR => Error
  }

  private def withResponse[A](op: Response[A] => Any): Response[A] = {
    val response = new Response[A]
    op(response)
    response
  }
}