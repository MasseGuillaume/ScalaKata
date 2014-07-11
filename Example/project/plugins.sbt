resolvers += Resolver.url(
  "masseguillaume",
    url("http://dl.bintray.com/content/masseguillaume/sbt-plugins"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("com.scalakata" % "plugin" % "0.1.0")