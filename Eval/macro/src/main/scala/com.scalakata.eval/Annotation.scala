package com.scalakata.eval

import scala.reflect.macros.whitebox.Context

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

// quasiquote list
// http://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html

object ScalaKataMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

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
        println(showCode(block))
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
        case ident: Ident ⇒ inst(ident)
        case lit: Literal ⇒ lit
        case apply @ q"$expr(..$params)" ⇒ inst(apply)
        case block @ q"{ ..$stats }" if (depth == 0) ⇒ instBlock(block, stats, depth)
        case select @ q"$expr.$name" ⇒ inst(select)
        case otherwise ⇒ otherwise
      }

      val bodyI = body.map(b => topInst(b))

      q"""
      object $name {
        private val $instr = scala.collection.mutable.Map.empty[(Int, Int), (String, RenderType)]

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
  def macroTransform(annottees: Any*) = macro ScalaKataMacro.impl
}
