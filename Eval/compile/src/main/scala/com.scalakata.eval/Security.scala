package com.scalakata.eval

import java.security._
import java.io._

object Security {
  val read1 = new java.io.FilePermission("../-", "read")
  val read2 = new java.io.FilePermission("../.", "read")

  class SecurityPolicy extends Policy {
    override def implies(domain: ProtectionDomain, permission: Permission) = {
      // not in eval
      Thread.currentThread().getStackTrace().find(_.getFileName == "(inline)").isEmpty ||
      read1.implies(permission) ||
      read2.implies(permission)
    }
  }

  def start: Unit = {
    Policy.setPolicy(new SecurityPolicy)
    System.setSecurityManager(new SecurityManager)
  }
}
