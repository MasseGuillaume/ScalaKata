package intro

object Extras {
  class Meter(val v: Int) extends AnyVal {
    def +(o: Meter) = new Meter(v + o.v)
  }
}

import com.scalakata.eval._
@ScalaKata object ValueClasses {
  import Extras._
  new Meter(1) + new Meter(2)
}
