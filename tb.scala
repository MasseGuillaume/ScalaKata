import scala.reflect.runtime.{currentMirror ⇒ cm}
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox
val tb = cm.mkToolBox()

tb.parse("1+1")
