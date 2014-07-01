package com.scalakata.eval

import scala.reflect.macros.whitebox.Context

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

// quasiquote list
// http://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html

object ScalaKataMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def instrument(body: Seq[Tree], name: c.universe.TermName, offset: Int) = {
      val instr = newTermName("instr$")

      implicit def lift = Liftable[c.universe.Position] { p =>
        q"(${p.point - offset}, ${p.end - offset})"
      }
      def inst(expr: Tree, rhs: Tree): Tree = {
        q"""
        {
          val t = $rhs
          ${instr}(${expr.pos}) = t
          t
        }
        """
      }

      val bodyI = body.map {
        case ident: Ident => inst(ident, ident)
        case expr @ q"val $pat = $exprV" => q"val $pat = ${inst(expr, exprV)}"
        case expr @ q"var $pat = $exprV" => q"var $pat = ${inst(expr, exprV)}"
        case select @ q"$expr.$name" => inst(select, select)
        case apply @ q"$expr(..$params)" => inst(apply, apply)
        case tree @ q"(..$exprs)" => inst(tree, tree)
        case block @ q"{ ..$stats }" => inst(block, block)
        case trycatch @ q"try $expr catch { case ..$cases }" => inst(trycatch, trycatch)
        case function @ q"(..$params) => $expr" => inst(function, function)
        case fort @ q"for (..$enums) $expr" => inst(fort, fort)
        case fory @ q"for (..$enums) yield $expr" => inst(fory, fory)
        case otherwise => otherwise
      }
      q"""
      object $name { 
        val $instr = scala.collection.mutable.Map.empty[(Int, Int), Any]

        def ${newTermName("eval$")}() = {
          ..$bodyI
          $instr
        }
      }
      """
    }

    val result: Tree = {
      annottees.map(_.tree).toList match {
        case q"object $name { ..$bodyO }" :: Nil => {
          bodyO match {
            case (obj @ q"object B { ..$body }") :: Nil => {
              instrument(body, name, obj.pos.point + 2)
            }
          }
        }
      }
    }
    c.Expr[Any](result)
  }
}

class ScalaKata extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro ScalaKataMacro.impl
}