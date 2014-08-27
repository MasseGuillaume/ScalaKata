package com.scalakata.eval

import scala.reflect.macros.whitebox.Context

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

import scala.collection.mutable.{Set ⇒ MSet}

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
    def instrument(body: Seq[Tree], name: c.TermName) = {
      val instr = TermName("instr$")

      implicit def lift = Liftable[c.universe.Position] { p ⇒
        q"(${p.point}, ${p.end})"
      }

      def inst(expr: Tree, renderSet: c.TermName): Tree = {
        val t = TermName("t$")
				q"""{
					val $t = $expr
					$renderSet += scala.Tuple2(${expr.pos}, render($t))
					$t
				}"""
      }

      def instBlock(block: Tree, childs: List[Tree], last: Tree, renderSet: c.universe.TermName, depth: Int): Tree = {
        val ts = TermName("ts$")
        val t = TermName("t$")
        q"""
        {
          val $ts = scala.collection.mutable.Set.empty[(Range, Render)]
          ..${childs.map(s => topInst(s, ts, depth + 1))}
          val t$$ = ${topInst(last, ts, depth + 1)}
          $renderSet += scala.Tuple2(${block.pos}, Block(${ts}.to[List].sortBy(_._1)))
          $ts
        }
        """
      }

      def topInst(tree: Tree, renderSet: c.universe.TermName, depth: Int = 0): Tree = tree match {
        case ident: Ident ⇒ inst(ident, renderSet) // a
        case apply @ q"$expr(..$params)" ⇒ inst(apply, renderSet) // f(..)
        case block @ Block(childs, last) if (depth == 0) ⇒ instBlock(block, childs, last, renderSet, depth) // { }
        case select @ q"$expr.$name" ⇒ inst(select, renderSet) // T.b
        case mat: Match ⇒ inst(mat, renderSet)
        case tr: Try ⇒ inst(tr, renderSet)

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

      q"""
      object $name {
        private val $instr = scala.collection.mutable.Set.empty[(Range, Render)]
        ..$bodyUI
        def ${TermName("eval$")}() = {
          ..${body.map(b => topInst(b, instr))}
          $instr
        }
      }
      """
    }

    c.Expr[Any]{
      annottees.map(_.tree).toList match {
        case q"object $name { ..$body }" :: Nil ⇒
          val res = instrument(body, name)
          /*println(showCode(res))
          println(showRaw(res))*/
          res
      }
    }
  }
}

class ScalaKata extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro ScalaKataMacro.instrumentation
}
