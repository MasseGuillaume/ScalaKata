resolvers += Resolver.url(
  "masseguillaume",
    url("http://dl.bintray.com/content/masseguillaume/sbt-plugins"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("com.scalakata" % "plugin" % "0.2.0")