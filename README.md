# Scala Kata

![scala kata editor with scalaz example](https://raw.githubusercontent.com/MasseGuillaume/ScalaKata/develop/example.png)
*hacking scalaz*

add the plugin to project/plugins.sbt

```scala
addSbtPlugin("com.scalakata" % "plugin" % "0.1.0")
```

and to your build definition build.sbt

```scala
seq(kataSettings: _*)
```

or in your Build.scala

```scala
com.scalakata.Scalakata.kataSettings
```