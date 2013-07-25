package bootstrap.liftweb

import com.scalakata._
import boot.WebJars
import model.ScalaEval
import security.ScalaKataSecurity
import service.{KataMongo, CompileService}
import snippet.KataResource

import net.liftweb._
import http._
import provider._
import sitemap._

class Boot {
  def boot {
    ScalaKataSecurity.start
    WebJars.serve
    KataMongo.start

   LiftRules.supplimentalHeaders = s => s.addHeaders(
    List(HTTPParam("X-Lift-Version", LiftRules.liftVersion),
      HTTPParam("Access-Control-Allow-Origin", "*"),
      HTTPParam("Access-Control-Allow-Credentials", "true"),
      HTTPParam("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS"),
      HTTPParam("Access-Control-Allow-Headers", "WWW-Authenticate,Keep-Alive,User-Agent,X-Requested-With,Cache-Control,Content-Type")
    ))

    LiftRules.statelessDispatch.prepend(CompileService.serve)
    LiftRules.statelessDispatch.prepend(KataResource.tddRedirect)

    LiftRules.ajaxPostTimeout = ScalaEval.timeBudget.toMillis.toInt
    LiftRules.addToPackages("com.scalakata")
    LiftRules.setSiteMap(SiteMap(
      KataResource.classic,
      KataResource.tdd
    ))
    LiftRules.autoIncludeAjaxCalc.default.set(() => (_: LiftSession) => false )
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
  }
}
