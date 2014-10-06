package com.scalakata.eval

import scala.reflect.macros.whitebox.Context

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

import collection.immutable.Queue
import collection.mutable.{Map => MMap}

case class Record[K: Ordering, V](inner: MMap[K, Queue[V]] = MMap.empty[K, Queue[V]]) {
  def update(k: K, nv: V): Unit = {
    val t =
      inner.get(k) match {
        case Some(v) ⇒ v :+ nv
        case None ⇒ Queue(nv)
      }
    inner(k) = t
  }
  def ordered: List[(K, List[V])] = {
    inner.toMap.mapValues(_.to[List]).to[List].sortBy(_._1)
  }
}

// quasiquote list
// http://docs.scala-lang.org/overviews/quasiquotes/syntax-summary.html

object ScalaKataMacro {
  private val instrName = "instr$"
  def desugar_impl[T](c: Context)(code: c.Expr[T]): c.Expr[com.scalakata.eval.Markdown2] = {
    import c.universe._
    val pre = s"""|```scala
                  |${showCode(code.tree)}
                  |```""".stripMargin
    c.Expr[com.scalakata.eval.Markdown2](q"com.scalakata.eval.Markdown2($pre)")
  }

  def trace_implf(c: Context): c.Expr[Any ⇒ Unit] = {
    import c.universe._
    implicit def lift = Liftable[c.universe.Position] { p ⇒
      q"(${p.point}, ${p.end})"
    }
    val instr = TermName(instrName)
    val t = TermName("t$")
    c.Expr[Any => Unit](q"""{
      (v: Any) => {
        ${instr}(${c.enclosingPosition}) = render(v)
        ()
      }
    }""")
  }

  def instrumentation(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    implicit def lift = Liftable[c.universe.Position] { p ⇒
      q"(${p.point}, ${p.end})"
    }

    // we modify a global mutable set of range position and representation
    // we can swap this set if we are inside a block
    // so we have easy access to it in the trace macro
    def instrument(body: Seq[Tree], name: c.TermName, extend: List[Tree]) = {
      val instr = TermName(instrName)

      def inst2(expr: Tree, posTree: Tree) : Tree = {
        val t = TermName("t$")
        q"""{
          val $t = $expr
          if(isNotUnit($t)) ${instr}(${posTree.pos}) = render($t)
          $t
        }"""
      }
      def inst(expr: Tree): Tree = inst2(expr, expr)

      def instBlock(block: Tree, childs: List[Tree], last: Tree, depth: Int): Tree = {
        val t = TermName("t$")
        val oldinstr = TermName("oldinstr$")
        q"""
        {
          val $oldinstr = $instr
          $instr = Record[Range, Render]()
          ..${childs.map(s => topInst(s, depth + 1))}
          val $t = ${topInst(last, depth + 1)}
          ${oldinstr}(${block.pos}) = Block(${instr}.ordered)
          $instr = $oldinstr
          $t
        }
        """
      }

      def topInst(tree: Tree, depth: Int = 0): Tree = tree match {
        case ident: Ident ⇒ inst(ident) // a
        case apply: Apply ⇒ inst(apply) // f(..)

        case block @ Block(childs, last) if (depth == 0) ⇒ instBlock(block, childs, last, depth)
        case select: Select ⇒ inst(select)
        case mat: Match ⇒ inst(mat)
        case tr: Try ⇒ inst(tr)
        case otherwise ⇒ otherwise
      }

      q"""
      object $name extends ..$extend {
        private var $instr = Record[Range, Render]()
        def ${TermName("eval$")}(): OrderedRender = {
          ..${body.map(b => topInst(b))}
          ${instr}.ordered
        }
      }
      """
    }

    c.Expr[Any]{
      annottees.map(_.tree).toList match {
        case q"object $name extends ..$extend { ..$body }" :: Nil ⇒
          instrument(body, name, extend)
      }
    }
  }
}

class ScalaKata extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro ScalaKataMacro.instrumentation
}
