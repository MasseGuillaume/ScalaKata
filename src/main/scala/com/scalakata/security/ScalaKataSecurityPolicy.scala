package com.scalakata.security

import java.security._
import java.io.FilePermission
import java.lang.reflect.ReflectPermission

/*
 * Really basic permission if you run code without
 * a source bail
 */
object ScalaKataSecurityPolicy extends Policy {
  private val websitePermissions = new Permissions
  websitePermissions.add(new AllPermission)

  private val scriptPermissions = new Permissions
  scriptPermissions.add( new FilePermission("-","read") ) // all read
  scriptPermissions.add( new RuntimePermission("accessDeclaredMembers") ) // reflexion
  scriptPermissions.add( new ReflectPermission("suppressAccessChecks") )
  scriptPermissions.add( new RuntimePermission("getenv.*") )

  override def getPermissions( sourceCode: CodeSource ) = {
    if(sourceCode.getLocation == null)
      scriptPermissions
    else
      websitePermissions
  }

  override def getPermissions( domain: ProtectionDomain ) = {
    if(domain.getCodeSource.getLocation == null)
      scriptPermissions
    else
      websitePermissions
  }
}