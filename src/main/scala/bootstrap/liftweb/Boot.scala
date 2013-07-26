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

    LiftRules.statelessDispatch.prepend(CompileService.serve)
    LiftRules.statelessDispatch.prepend(KataResource.tddRedirect)
    LiftRules.statelessDispatch.prepend(CompileService.coors)

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

/*
$.ajax({
  url: "http://localhost:8080/scala/api",
  type: "POST",
  contentType: "application/json; charset=utf-8",
  dataType: "json"
})
*/