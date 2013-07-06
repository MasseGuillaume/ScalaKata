package com.scalakata.security

import java.security._
import net.liftweb.util.Props

object ScalaKataSecurity {
  def start: Unit = {
//
//    Props.mode match {
//      case Props.RunModes.Production => {
        Policy.setPolicy( ScalaKataSecurityPolicy )
        System.setSecurityManager( new SecurityManager( ) )
//      }
//      case _ => ()
//    }
  }
}