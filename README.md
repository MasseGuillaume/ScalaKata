# Scala Kata [![Build Status](https://api.travis-ci.org/MasseGuillaume/ScalaKata.png?branch=master)](https://travis-ci.org/MasseGuillaume/ScalaKata) [![Stories in Ready](https://badge.waffle.io/MasseGuillaume/ScalaKata.png?label=Ready)](https://waffle.io/MasseGuillaume/ScalaKata)

![scala kata editor with scalaz example](https://raw.githubusercontent.com/MasseGuillaume/ScalaKata/develop/example.png)
*hacking scalaz*

add the plugin to project/plugins.sbt

```scala
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