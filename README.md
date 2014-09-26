# Scala Kata [![Gittip](http://img.shields.io/gittip/MasseGuillaume.svg?style=flat)](https://www.gittip.com/MasseGuillaume/) [![Build Status](http://img.shields.io/travis/MasseGuillaume/ScalaKata.svg?style=flat)](https://travis-ci.org/MasseGuillaume/ScalaKata) [![Stories in Ready](https://badge.waffle.io/MasseGuillaume/ScalaKata.png?label=Ready)](https://waffle.io/MasseGuillaume/ScalaKata)

![scala kata instructor mode](https://raw.githubusercontent.com/MasseGuillaume/ScalaKata/develop/Examples/Instructor.png)
*Instructor Mode*

add the plugin to project/plugins.sbt

```scala
addSbtPlugin("com.scalakata" % "plugin" % "0.8.0")
```

and to your build definition build.sbt

```scala
seq(kataSettings: _*)
```

or in your Build.scala

```scala
com.scalakata.Scalakata.kataSettings
```

start with ```sbt kstart```

## Docker

It's also possible to run scala kata in a docker container:

```
sudo docker run -p 7331:7331 --name scalakata masseguillaume/scalakata:0.8.0
```
