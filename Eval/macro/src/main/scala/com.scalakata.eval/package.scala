package com.scalakata

import scala.language.experimental.macros
import collection.immutable.Queue

package object eval {
  type Range = (Int, Int)
  type OrderedRender = List[(Range, List[Render])]

  sealed trait Render

  sealed trait Expression extends Render
  case class EString(v: String) extends Expression
  case class Other(repr: String) extends Expression

  case class Block(childs: OrderedRender ) extends Render
  case class Steps(simplifications: List[Expression]) extends Render

  case class Latex(a: String) extends Render {
    override def toString = a
    def stripMargin = Latex(a.stripMargin)
  }

  case class Markdown(a: String) extends Render {
    override def toString = a
    def stripMargin = Markdown(a.stripMargin)
  }

  case class Html(a: String) extends Render {
    override def toString = a
    def stripMargin = Html(a.stripMargin)
  }

  def isNotUnit(a: Any) = {
    a match {
      case _: Unit ⇒ false
      case _ ⇒ true
    }
  }
  def render[A >: Null](a: A): Render = {
  	a match {
    	case null ⇒ Other("null")
      case ar: Array[_] ⇒ Other(ar.deep.toString)
      case v: String ⇒  EString(v)
      case _: scala.xml.Elem ⇒ Html(a.toString)
      case l: Latex ⇒ l
      case md: Markdown ⇒ md
      case h: Html ⇒ h
      case other ⇒ Other(other.toString)
    }
  }

  implicit class LatexHelper(val sc: StringContext) extends AnyVal {
    def latex(args: Any*): Latex = {
      Latex(sc.raw(args: _*))
    }
    def tex(args: Any*) = latex(args: _*)
  }
  implicit class MarkdownHelper(val sc: StringContext) extends AnyVal {
    def markdown(args: Any*): Markdown = {
      Markdown(sc.s(args: _*))
    }
    def md(args: Any*) = markdown(args: _*)
    def markdownR(args: Any*): Markdown = {
      Markdown(sc.raw(args: _*))
    }
    def mdR(args: Any*) = markdownR(args: _*)
  }
  implicit class HtmlHelper(val sc: StringContext) extends AnyVal {
    def html(args: Any*): Html = {
      Html(sc.s(args: _*))
    }
    def htmlR(args: Any*): Html = {
      Html(sc.raw(args: _*))
    }
  }

  def desugar[T](code: T): String = macro ScalaKataMacro.desugar_impl[T]

  def trace: Any => Unit = macro ScalaKataMacro.trace_implf
  def print: Any => Unit = macro ScalaKataMacro.trace_implf
  def println: Any => Unit = macro ScalaKataMacro.trace_implf
  def oprintln: Any => Unit = (a) => scala.Predef.println(a)
}
