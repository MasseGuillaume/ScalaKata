package com.scalakata.eval

import scala.reflect.macros.whitebox.Context

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

// quasiquote list
// http://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html

sealed trait RenderType
final case object Html extends RenderType
final case object Latex extends RenderType
final case object RString extends RenderType
final case object Other extends RenderType

object ScalaKataMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def instrument(body: Seq[Tree], name: c.universe.TermName) = {
      val instr = TermName("instr$")

      implicit def lift = Liftable[c.universe.Position] { p ⇒
        q"(${p.point}, ${p.end})"
      }
      def inst(expr: Tree, rhs: Tree): List[Tree] = {
        List(expr, q"${instr}(${expr.pos}) = ScalaKata.render($rhs)")
      }
      def inst2(expr: Tree, rhs: TermName): List[Tree] = {
        List(expr, q"${instr}(${expr.pos}) = ScalaKata.render($rhs)")
      }

      val bodyI = body.flatMap {
        case ident: Ident ⇒ inst(ident, ident)
        case expr @ q"val $pat = $exprV" ⇒ inst2(expr, pat)
        case expr @ q"var $pat = $exprV" ⇒ inst2(expr, pat)
        case select @ q"$expr.$name" ⇒ inst(select, select)
        case apply @ q"$expr(..$params)" ⇒ inst(apply, apply)
        case tree @ q"(..$exprs)" ⇒ inst(tree, tree)
        case block @ q"{ ..$stats }" ⇒ inst(block, block)
        case trycatch @ q"try $expr catch { case ..$cases }" ⇒ inst(trycatch, trycatch)
        case function @ q"(..$params) ⇒ $expr" ⇒ inst(function, function)
        case fort @ q"for (..$enums) $expr" ⇒ inst(fort, fort)
        case fory @ q"for (..$enums) yield $expr" ⇒ inst(fory, fory)
        case otherwise ⇒ List(otherwise)
      }
      q"""
      object $name {
        val $instr = scala.collection.mutable.Map.empty[(Int, Int), (String, RenderType)]

        def ${TermName("eval$")}() = {
          ..$bodyI
          $instr
        }
      }
      """
    }

    c.Expr[Any]{
      annottees.map(_.tree).toList match {
        case q"object $name { ..$body }" :: Nil ⇒
          val res = instrument(body, name)
          //println(showCode(res))
          res
      }
    }
  }
}

class ScalaKata extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro ScalaKataMacro.impl
}

object ScalaKata {
  def render(a: Any): (String, RenderType) = {
    val tpe = a match {
      case a: String ⇒  RString
      case xml: scala.xml.Elem ⇒ Html

      // TODO: Markdown
      // case m: Markdown  ⇒

      // TODO: Latex
      // case l: Latex ⇒

      case other ⇒ Other
    }
    (a.toString, tpe)
  }
}
