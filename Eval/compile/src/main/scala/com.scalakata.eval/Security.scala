package com.scalakata.eval

import java.security._
import java.io._

object Security {
  class SecurityPolicy extends Policy {
    override def implies(domain: ProtectionDomain, permission: Permission) = {
      // not in eval
      Thread.currentThread().getStackTrace().find(_.getFileName == "(inline)").isEmptyy
    }
  }

  def start: Unit = {
    Policy.setPolicy(new SecurityPolicy)
    System.setSecurityManager(new SecurityManager)
  }
}
