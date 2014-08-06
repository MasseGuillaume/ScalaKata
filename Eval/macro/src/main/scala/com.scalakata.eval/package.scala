package com.scalakata

package object eval {

  sealed trait RenderType
  final case object RT_Html extends RenderType
  final case object RT_Latex extends RenderType
  final case object RT_Markdown extends RenderType
  final case object RT_String extends RenderType
  final case object RT_Other extends RenderType

  case class Latex(a: String) {
    override def toString = a
  }
  implicit class LatexHelper(val sc: StringContext) extends AnyVal {
    def latex(args: Any*): Latex = {
      Latex(sc.raw(args: _*))
    }
    def tex(args: Any*) = latex(args: _*)
  }

  case class Markdown(a: String) {
    override def toString = a
  }
  implicit class MarkdownHelper(val sc: StringContext) extends AnyVal {
    def markdown(args: Any*): Markdown = {
      Markdown(sc.s(args: _*))
    }
    def md(args: Any*) = markdown(args: _*)
  }

  case class Html(a: String) {
    override def toString = a
  }
  implicit class HtmlHelper(val sc: StringContext) extends AnyVal {
    def html(args: Any*): Html = {
      Html(sc.s(args: _*))
    }
  }

  def render(a: AnyRef): (String, RenderType) = {
    if(a eq null) ("null", RT_Other)
    else {
      val tpe = a match {
        case _: String ⇒  RT_String
        case _: scala.xml.Elem ⇒ RT_Html
        case _: Latex => RT_Latex
        case _: Markdown => RT_Markdown
        case _: Html => RT_Html
        case other ⇒ RT_Other
      }
      (a.toString, tpe)
    }
  }
}
