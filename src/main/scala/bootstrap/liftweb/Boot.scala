package bootstrap.liftweb

import com.scalakata._
import boot.WebJars
import service.CompileService
import model.ScalaEval

import net.liftweb._
import http._
import sitemap._
import com.scalakata.security.ScalaKataSecurity

class Boot {
  def boot {
    WebJars.serve
    ScalaKataSecurity.start

    LiftRules.statelessDispatch.prepend(CompileService)

    LiftRules.ajaxPostTimeout = ScalaEval.timeBudget.toMillis.toInt

    LiftRules.addToPackages("com.scalakata")
    LiftRules.setSiteMap(SiteMap(
      Menu.i("Home") / "index"
    ))

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
  }
}
