package com.scalakata.model

import eval._

import scala.reflect.internal.util.Position

import java.security.AccessControlException

import scala.concurrent.duration._
import java.util.concurrent.{TimeoutException, Callable, FutureTask, TimeUnit}

object ScalaEval {
  def apply( code: String ) = {
    evalWithin(timeBudget.toMillis){
      val baos = new java.io.ByteArrayOutputStream
      try {
        val result = 
          Console.withOut(baos) { eval[Any](code) } match {
            case n: Unit => ""
            case e => e
          }

        Compile( result.toString, baos.toString )
      } catch {
        case CompilerException(errors: List[(Position,String,Int)]) => {
          CompileError(errors)
        }
        case e: AccessControlException => {
          SecurityError(e.toString)
        }
      }
    }
  }

  def evalWithin(timeout: Long)(f: => EvalResult): EvalResult = {
    val task = new FutureTask( new Callable[EvalResult]( ) {
      def call = f
    })
    val thread = new Thread( task )
    try {
      thread.start()
      task.get(timeout, TimeUnit.MILLISECONDS)
    } catch {
      case e: TimeoutException => EvalTimeout( timeBudget.toString )
      case ex: Throwable => UnknownError( ex.getMessage )
    } finally { 
      if( thread.isAlive ){
        thread.interrupt()
        thread.stop()
      }
    }
  }
  val timeBudget = 60.seconds
  private val eval = new Eval(None)
}

sealed abstract class EvalResult
case class Compile(result: String, console: String) extends EvalResult
case class CompileError(errors: List[(Position,String,Int)]) extends EvalResult
case class SecurityError(error: String) extends EvalResult
case class EvalTimeout(timeout: String) extends EvalResult
case class UnknownError(cause: String) extends EvalResult