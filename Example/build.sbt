scalaVersion := "2.11.2"

seq(kataSettings: _*)

initialCode in Kata := (
"""|class Meter(val v: Int) extends AnyVal {
   |	def +(m: Meter) = new Meter(v + m.v)
   |}""".stripMargin,
"""|List(1, 2)
   |new Meter(1) + new Meter(2)""".stripMargin
)
