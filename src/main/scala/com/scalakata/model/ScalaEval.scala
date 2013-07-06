package com.scalakata.model

import eval._

import net.liftweb.json._
import JsonAST._
import JsonDSL._

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


object JsonPrinter {
  def print(result: EvalResult): JValue = {
    result match {
      case Compile(result,console) => ("result" -> result) ~ ("console" -> console)
      case CompileError(errors) => {
        val jErrors: JValue = errors.map { case ( position, message, severity ) => {
         val severityLabel = severity match {
           case 0 => "info"
           case 1 => "warning"
           case 2 => "error"
         }

          val line = position.line - 3

          val tabCount = position.inUltimateSource( position.source ).lineContent.count( _ == '\t' )
          val column = ( position.column - tabCount * Position.tabInc )

          (
            ("line" -> line) ~
            ("column" -> column) ~
            ("message" -> message) ~
            ("severity" -> severityLabel)
          )
          
        }}
        "errors" -> jErrors
      }
      case EvalTimeout(timeout) => ("error" -> s"computation cannot exceed $timeout")
      case SecurityError(error) => ("error" -> s"security error: $error")
      case UnknownError(cause) => ("error" -> s"unknown error: $cause")
    }
  }
}