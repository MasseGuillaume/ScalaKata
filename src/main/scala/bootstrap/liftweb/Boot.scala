package bootstrap.liftweb

import com.scalakata._
import boot.WebJars
import service.CompileService

import net.liftweb._
import http._
import sitemap._

class Boot {
  def boot {
    WebJars.serve

    LiftRules.statelessDispatch.prepend(CompileService)

    LiftRules.addToPackages("com.scalakata")
    LiftRules.setSiteMap(SiteMap(
      Menu.i("Home") / "index"
    ))

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
  }
}
