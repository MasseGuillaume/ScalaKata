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
          case Some(v) => v :+ nv
          case None => Queue(nv)
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
  def desugar_impl[T](c: Context)(code: c.Expr[T]): c.Expr[String] = {
    import c.universe._
    c.Expr[String](q"${showCode(code.tree)}")
  }

  def trace_implf(c: Context): c.Expr[Any => Unit] = {
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
    def instrument(body: Seq[Tree], name: c.TermName) = {
      val instr = TermName(instrName)

      def inst(expr: Tree): Tree = {
        val t = TermName("t$")
				q"""{
					val $t = $expr
          if(isNotUnit($t)) ${instr}(${expr.pos}) = render($t)
					$t
				}"""
      }

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
        case apply @ q"$expr(..$params)" ⇒ inst(apply) // f(..)
        case block @ Block(childs, last) if (depth == 0) ⇒ instBlock(block, childs, last, depth) // { }
        case select @ q"$expr.$name" ⇒ inst(select) // T.b
        case mat: Match ⇒ inst(mat)
        case tr: Try ⇒ inst(tr)

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
        case o: ModuleDef ⇒ o // objects
        case c: ClassDef ⇒ c
      }

      q"""
      object $name {
        private var $instr = Record[Range, Render]()
        ..$bodyUI
        def ${TermName("eval$")}(): OrderedRender = {
          ..${body.map(b => topInst(b))}
          ${instr}.ordered
        }
      }
      """
    }

    c.Expr[Any]{
      annottees.map(_.tree).toList match {
        case q"object $name { ..$body }" :: Nil ⇒
          val res = instrument(body, name)
          /*scala.Predef.println(showCode(res))*/
          /*println(showRaw(res))*/
          res
      }
    }
  }
}

class ScalaKata extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro ScalaKataMacro.instrumentation
}
