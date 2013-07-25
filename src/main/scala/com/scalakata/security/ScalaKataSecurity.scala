package com.scalakata.security

import java.security._
import net.liftweb.util.Props

object ScalaKataSecurity {
  def start: Unit = {
    Policy.setPolicy( ScalaKataSecurityPolicy )
    System.setSecurityManager( new SecurityManager( ) )
  }
}