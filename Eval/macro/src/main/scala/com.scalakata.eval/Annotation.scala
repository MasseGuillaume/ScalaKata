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

class Helper[C <: Context](val c: C) {
  import c.universe._

  def lift(p: c.universe.Position): (Int, Int) = {
    (p.start, p.end)
  }
  implicit def liftq = Liftable[c.universe.Position] { p ⇒
    q"(${p.point}, ${p.end})"
  }

  def topInst(tree: Tree, depth: Int = 0)(instr: TermName): Tree = tree match {
    case t @ q"desugar{ ..$body }" ⇒ block(lift(t.pos), body, depth, true)(instr)
    case ident: Ident ⇒ inst(ident)(instr) // a
    case apply: Apply ⇒ inst(apply)(instr) // f(..)

    case bl @ Block(childs, last) if (depth == 0) ⇒ block(lift(bl.pos), childs ::: List(last), depth)(instr)
    case select: Select ⇒ inst(select)(instr)
    case mat: Match ⇒ inst(mat)(instr)
    case tr: Try ⇒ inst(tr)(instr)
    case c: ClassDef ⇒ q""
    case m: ModuleDef ⇒ q""
    case otherwise ⇒ otherwise
  }

  def block(position: (Int, Int), childs: List[Tree], depth: Int, desug: Boolean = false)(instr: TermName): Tree = {
    val t = TermName("t$")
    val oldinstr = TermName("oldinstr$")

    def des(tree: Tree): Tree = {
      val pre = s"""|```scala
                    |${showCode(tree)}
                    |```""".stripMargin
      q"${instr}(${tree.pos}) = com.scalakata.eval.Markdown2($pre)"
    }

    val childs2 = childs.flatMap{ s =>
      val ins = topInst(s, depth + 1)(instr)
      if(desug) List(des(s), ins)
      else List(ins)
    }

    q"""{
      val $oldinstr = $instr
      $instr = Record[Range, Render]()
      ..$childs2
      ${oldinstr}($position) = Block(${instr}.ordered)
      $instr = $oldinstr
      ()
    }"""
  }

  def inst2(expr: Tree, posTree: Tree)(instr: TermName) : Tree = {
    val t = TermName("t$")
    q"""{
      val $t = $expr
      if(isNotUnit($t)) ${instr}(${posTree.pos}) = render($t)
      $t
    }"""
  }
  def inst(expr: Tree)(instr: TermName): Tree = inst2(expr, expr)(instr)

  def trace(instr: TermName) = {
    val t = TermName("t$")
    c.Expr[Any => Unit](q"""{
      (v: Any) => {
        ${instr}(${c.enclosingPosition}) = render(v)
        ()
      }
    }""")
  }
}

object ScalaKataMacro {
  val instrName = "instr$"

  def trace_implf(c: Context): c.Expr[Any ⇒ Unit] = {
    val instr = c.universe.TermName(instrName)
    new Helper[c.type](c).trace(instr)
  }

  def desugar2_impl[T](c: Context)(code: c.Expr[T]): c.Expr[Unit] = {
    import c.universe._
    val instr = TermName(instrName)

    val q"{ ..$body }" = code.tree

    val pos =
      if(body.isEmpty) (code.tree.pos.start, code.tree.pos.end)
      else (body.head.pos.start, body.last.pos.end)

    c.Expr[Unit](
      new Helper[c.type](c).block(pos, body, 0, true)(instr)
    )
  }

  def instrumentation(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    // we modify a global mutable set of range position and representation
    // we can swap this set if we are inside a block
    // so we have easy access to it in the trace macro
    def instrument(body: Seq[Tree], name: c.TermName, extend: List[Tree]) = {
      val instr = TermName(instrName)

      val classAndObjects = body.collect {
        case c: ClassDef ⇒ c
        case m: ModuleDef ⇒ m
      }

      val helper = new Helper[c.type](c)
      q"""
      object $name extends ..$extend {
        ..$classAndObjects
        private var $instr = Record[Range, Render]()
        def ${TermName("eval$")}(): OrderedRender = {
          ..${body.map(b => helper.topInst(b)(instr))}
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
