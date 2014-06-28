package com.scalakata.eval

import scala.reflect.macros.blackbox.Context

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

// quasiquote list
// http://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html

object ScalaKataMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._


    val result: Tree = {
      val eval = newTermName("eval$")
      annottees.map(_.tree).toList match {
        case q"object $name { ..$body }" :: Nil => {
          val instr = newTermName("instr$")
          implicit def lift = Liftable[c.universe.Position] { p =>
            q"(${p.point}, ${p.end})"
          }
          def instrument(rhs: Tree): Tree = {
            q"""
            {
              val t = $rhs
              ${instr}(${rhs.pos}) = t
              t
            }
            """
          }

          val bodyI = body.map {
            case ident: Ident => instrument(ident)
            case q"val $pat = $expr" => q"val $pat = ${instrument(expr)}"
            case q"var $pat = $expr" => q"var $pat = ${instrument(expr)}"
            case select @ q"$expr.$name" => instrument(select)
            case apply @ q"$expr(..$params)" => instrument(apply)
            case tree @ q"(..$exprs)" => instrument(tree)
            case block @ q"{ ..$stats }" => instrument(block)
            case trycatch @ q"try $expr catch { case ..$cases }" => instrument(trycatch)
            case function @ q"(..$params) => $expr" => instrument(function)
            case fort @ q"for (..$enums) $expr" => instrument(fort)
            case fory @ q"for (..$enums) yield $expr" => instrument(fory)
            case otherwise => otherwise
          }
          q"""
          object $name { 
            val $instr = scala.collection.mutable.Map.empty[(Int, Int), Any]

            def $eval() = {
              ..$bodyI
              $instr
            }
          }
          """
        }
      }
    }
    c.Expr[Any](result)
  }
}

class ScalaKata extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro ScalaKataMacro.impl
}