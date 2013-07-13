resolvers += Resolver.url("untyped",url("http://ivy.untyped.com/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.3.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

addSbtPlugin("com.untyped" %% "sbt-mustache" % "0.5")

addSbtPlugin("com.untyped" %% "sbt-js" % "0.6-M5")

addSbtPlugin("com.untyped" %% "sbt-less" % "0.6-M5")