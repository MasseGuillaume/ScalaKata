object Extras {
  class Meter(val v: Int) extends Anyval {
    def +(o: Meter) = new Meter(v + o.v)
  }
}

import com.scalakata.eval._
@ScalaKata object A {
  new Meter(1) + new Meter(2)
}
