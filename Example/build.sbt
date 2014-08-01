scalaVersion := "2.11.2"

seq(kataSettings: _*)

initialCode in Kata := (
"""|object Extras {
   |  class Meter(val v: Int) extends AnyVal {
   |	 def +(m: Meter) = new Meter(v + m.v)
   |  }
   |}""".stripMargin,
"""|List(1, 2)
   |
   |import Extras._
   |new Meter(1) + new Meter(2)
   |
   |latex""".stripMargin + """"""""" + """
   |\begin{align}
   |  E_0 &= mc^2                              \\
   |  E &= \frac{mc^2}{\sqrt{1-\frac{v^2}{c^2}}}
   |\end{align}
   |""".stripMargin + """""""""
)
