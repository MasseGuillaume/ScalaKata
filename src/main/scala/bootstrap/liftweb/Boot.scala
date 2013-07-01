package bootstrap.liftweb

import net.liftweb._
import http._
import sitemap._
import com.scalakata.boot.WebJars

class Boot {
  def boot {
    WebJars.serve

    LiftRules.addToPackages("com.scalakata")
    LiftRules.setSiteMap(SiteMap(
      Menu.i("Home") / "index"
    ))

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
  }
}
