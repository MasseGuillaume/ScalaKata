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

class Compiler(artifacts: String, scalacOptions: Seq[String]) {

  def insight(code: String): EvalResponse = {
    if (code.isEmpty) EvalResponse.empty
    else {
      try {
        withTimeout{
          eval(code) match {
            case (Some(instr), cinfos) ⇒
              val i =
                instr.map{ case ((start, end), (repr, tpe)) ⇒
                  Instrumentation(repr, tpe, start, end)
                }.to[List]

              EvalResponse.empty.copy(
                insight = i,
                infos = convert(cinfos)
              )
            case (_, cinfos) ⇒ EvalResponse.empty.copy(infos = convert(cinfos))
          }

        }(timeout).getOrElse(
          EvalResponse.empty.copy(timeout = true)
        )
      } catch {
        case NonFatal(e) ⇒ {
          val pos =
            if(e.getCause != null) {
              e.getCause.getStackTrace().find(_.getFileName == "(inline)").map(_.getLineNumber).getOrElse(1)
            } else {
              e.getStackTrace()(0).getLineNumber
            }

          EvalResponse.empty.copy(runtimeError =
            Some(RuntimeError(e.getCause.toString, pos))
          )
        }
      }
    }
  }

  private def reload(code: String): BatchSourceFile = {
    val file = new BatchSourceFile("default", code)
    withResponse[Unit](r ⇒ compiler.askReload(List(file), r)).get
    file
  }

  private def withResponse[A](op: Response[A] ⇒ Any): Response[A] = {
    val response = new Response[A]
    op(response)
    response
  }

  def autocomplete(code: String, p: Int): List[CompletionResponse] = {
    def completion(f: (compiler.Position, compiler.Response[List[compiler.Member]]) ⇒ Unit,
                   pos: compiler.Position):
                   List[CompletionResponse] = {

      withResponse[List[compiler.Member]](r ⇒ f(pos, r)).get match {
        case Left(members) ⇒ compiler.ask(() ⇒ {
          members.map(member ⇒
            CompletionResponse(
              name = member.sym.decodedName,
              signature = member.sym.signatureString
            )
          )
        })
        case Right(e) ⇒
          e.printStackTrace
          Nil
      }
    }
    def typeCompletion(pos: compiler.Position) = {
      completion(compiler.askTypeCompletion _, pos)
    }

    def scopeCompletion(pos: compiler.Position) = {
      completion(compiler.askScopeCompletion _, pos)
    }

    // inspired by scala-ide
    // https://github.com/scala-ide/scala-ide/blob/4.0.0-m3-luna/org.scala-ide.sdt.core/src/org/scalaide/core/completion/ScalaCompletions.scala#L170
    askTypeAt(code, p, p) { (tree, pos) ⇒ tree match {
      case compiler.New(name) ⇒ typeCompletion(name.pos)
      case compiler.Select(qualifier, _) if qualifier.pos.isDefined && qualifier.pos.isRange ⇒
        typeCompletion(qualifier.pos)
      case compiler.Import(expr, _) ⇒ typeCompletion(expr.pos)
      case compiler.Apply(fun, _) ⇒
        fun match {
          case compiler.Select(qualifier: compiler.New, _) ⇒ typeCompletion(qualifier.pos)
          case compiler.Select(qualifier, _) if qualifier.pos.isDefined && qualifier.pos.isRange ⇒
            typeCompletion(qualifier.pos)
          case _ ⇒ scopeCompletion(fun.pos)
        }
      case _ ⇒ scopeCompletion(pos)
    }}{
      pos => Some(scopeCompletion(pos))
    }.getOrElse(Nil)
  }

  def typeAt(code: String, start: Int, end: Int): Option[TypeAtResponse] = {
    askTypeAt(code, start, end){(tree, _) ⇒ {
      // inspired by ensime
      val res =
        tree match {
          case compiler.Select(qual, name) ⇒ qual
          case t: compiler.ImplDef if t.impl != null ⇒ t.impl
          case t: compiler.ValOrDefDef if t.tpt != null ⇒ t.tpt
          case t: compiler.ValOrDefDef if t.rhs != null ⇒ t.rhs
          case t ⇒ t
        }
      TypeAtResponse(res.tpe.toString)
    }}{Function.const(None)}
  }

  private def askTypeAt[A]
    (code: String, start: Int, end: Int)
    (f: (compiler.Tree, compiler.Position) ⇒ A)
    (fb: compiler.Position ⇒ Option[A]): Option[A] = {

    if(code.isEmpty) None
    else {
      val file = reload(code)
      val rpos = compiler.rangePos(file, start, start, end)

      val response = withResponse[compiler.Tree](r ⇒
        compiler.askTypeAt(rpos, r)
      )

      response.get match {
        case Left(tree) ⇒ Some(f(tree, rpos))
        case Right(e) ⇒ e.printStackTrace; fb(rpos)
      }
    }
  }

  private val timeout = 60.seconds
  private val jvmId = java.lang.Math.abs(new Random().nextInt())

  private val reporter = new StoreReporter()
  private val settings = new Settings()

  settings.processArguments(scalacOptions.to[List], true)
  settings.bootclasspath.value = artifacts
  settings.classpath.value = artifacts
  settings.Yrangepos.value = true

  private lazy val compiler = new Global(settings, reporter)
  private lazy val eval = new Eval(settings.copy)

  private def convert(infos: Map[String, List[(Int, String)]]): Map[Severity, List[CompilationInfo]] = {
    infos.map{ case (k,vs) ⇒
      val sev = k match {
        case "ERROR" ⇒ Error
        case "WARNING" ⇒ Warning
        case "INFO" ⇒ Info
      }
      (sev, vs map {case (p, m) ⇒ CompilationInfo(m, p)})
    }
  }

  private def withTimeout[T](f: ⇒ T)(timeout: Duration): Option[T]= {
    val task = new FutureTask(new Callable[T]() {
      def call = f
    })
    val thread = new Thread( task )
    try {
      thread.start()
      Some(task.get(timeout.toMillis, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException ⇒ None
    } finally {
      if( thread.isAlive ){
        thread.interrupt()
        thread.stop()
      }
    }
  }
}
