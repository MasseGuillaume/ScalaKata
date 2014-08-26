package com.scalakata.eval

import scala.reflect.macros.whitebox.Context

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

// quasiquote list
// http://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html

object ScalaKataMacro {

  def desugar_impl[T](c: Context)(code: c.Expr[T]): c.Expr[String] = {
    import c.universe._
    c.Expr[String](q"${showCode(code.tree)}")
  }

  def instrumentation(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    // we modify a global mutable map from range position to representation
    def instrument(body: Seq[Tree], name: c.universe.TermName) = {
      val instr = TermName("instr$")

      implicit def lift = Liftable[c.universe.Position] { p ⇒
        q"(${p.point}, ${p.end})"
      }

      def inst(expr: Tree): Tree = {
        q"""
        {
          val t = $expr
          ${instr}(${expr.pos}) = render(t)
          t
        }
        """
      }

      def instBlock(block: Tree, stats: List[Tree], depth: Int): Tree = {
        val inner = stats.map(s => topInst(s, depth + 1))
        q"""
        {
          val t = { ..$inner }
          ${instr}(${block.pos}) = (render(t)._1, RT_Block)
          t
        }
        """
      }

      def topInst(tree: Tree, depth: Int = 0): Tree = tree match {
        case ident: Ident ⇒ inst(ident) // a
        case apply @ q"$expr(..$params)" ⇒ inst(apply) // f(..)
        case block @ q"{ ..$stats }" if (depth == 0) ⇒ instBlock(block, stats, depth) // { }
        case select @ q"$expr.$name" ⇒ inst(select) // T.b
        case _: ValDef ⇒ q""
        case _: DefDef ⇒ q""
        case _: TypeDef ⇒ q""
        case _: Import ⇒ q""
        case otherwise ⇒ otherwise
      }

      // we extract val, def, etc so they can be used outside the instrumentation
      val bodyUI = body.collect{
        case vd: ValDef ⇒ vd
        case dd: DefDef ⇒ dd
        case td: TypeDef ⇒ td
        case i: Import ⇒ i
      }

      val bodyI = body.map(b => topInst(b))

      q"""
      object $name {
        private val $instr = scala.collection.mutable.Map.empty[(Int, Int), (String, RenderType)]
        ..$bodyUI
        def ${TermName("eval$")}() = {
          ..$bodyI
          $instr
        }
      }
      """
    }

    c.Expr[Any]{
      annottees.map(_.tree).toList match {
        case q"object $name { ..$body }" :: Nil ⇒ instrument(body, name)
      }
    }
  }
}

class ScalaKata extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro ScalaKataMacro.instrumentation
}
