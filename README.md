# Scala Kata

![scala kata editor with scalaz example](https://raw.githubusercontent.com/MasseGuillaume/ScalaKata/develop/example.png)
*hacking scalaz*

add the plugin to project/plugins.sbt

```scala
resolvers += Resolver.url(
  "masseguillaume",
    url("http://dl.bintray.com/content/masseguillaume/sbt-plugins"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("com.scalakata" % "plugin" % "0.2.0")
```

and to your build definition build.sbt

```scala
seq(kataSettings: _*)
```

or in your Build.scala

```scala
com.scalakata.Scalakata.kataSettings
```

start with ```kstart```