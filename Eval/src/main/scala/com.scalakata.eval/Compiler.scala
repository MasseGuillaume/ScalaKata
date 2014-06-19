package com.scalakata.eval

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Random
import java.util.concurrent.{TimeoutException, Callable, FutureTask, TimeUnit}

import scala.util.control.NonFatal
import scala.concurrent.duration._

import scala.tools.nsc.interactive.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.io.VirtualDirectory
import scala.reflect.internal.util._
import scala.tools.nsc.interactive.Response

class Compiler {

  def insight(code: String): EvalResponse = {
    if (code.isEmpty) EvalResponse.empty
    else {
      try { 
        withTimeout{
          eval(code) match {
            case (Some(v), cinfos) => 
              EvalResponse.empty.copy(
                insight = List(Instrumentation(v.toString, 0)),
                infos = convert(cinfos)
              )
            case (_, cinfos) => EvalResponse.empty.copy(infos = convert(cinfos))
          }

        }(timeout).getOrElse(
          EvalResponse.empty.copy(timeout = true)
        )
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
          EvalResponse.empty.copy(runtimeError = error)
        }
      }
    }
  }

  def autocomplete(code: String, pos: Int): List[CompletionResponse] = {

    val beginWrap = "object ScalaKata {\n"
    val endWrap = "\n}"

    val wrapOffset = beginWrap.size

    def wrap(code: String): BatchSourceFile = {
      new BatchSourceFile("default", beginWrap + code + endWrap)
    }

    def reload(code: String): BatchSourceFile = {
      val file = wrap(code)
      withResponse[Unit](r => compiler.askReload(List(file), r)).get
      file
    }

    def withResponse[A](op: Response[A] => Any): Response[A] = {
      val response = new Response[A]
      op(response)
      response
    }

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

  private val timeout = 60.seconds
  private val jvmId = java.lang.Math.abs(new Random().nextInt())

  private val reporter = new StoreReporter()
  private val settings = new Settings()
  
  private val artifacts = sbt.BuildInfo.dependencyClasspath.
    map(_.getAbsoluteFile).
    mkString(File.pathSeparator)
  
  settings.bootclasspath.value = artifacts
  settings.classpath.value = artifacts

  private val compiler = new Global(settings, reporter)
  private val eval = new Eval(artifacts)

  private def convert(infos: Map[String, List[(Int, String)]]): Map[Severity, List[CompilationInfo]] = {
    infos.map{ case (k,vs) => 
      val sev = k match {
        case "ERROR" => Error
        case "WARNING" => Warning
        case "INFO" => Info
      }
      (sev, vs map {case (p, m) => CompilationInfo(m, p)})
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
}