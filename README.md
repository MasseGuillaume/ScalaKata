# Scala Kata [![Gittip](http://img.shields.io/gittip/MasseGuillaume.svg?style=flat)](https://www.gittip.com/MasseGuillaume/) [![Build Status](http://img.shields.io/travis/MasseGuillaume/ScalaKata.svg?style=flat)](https://travis-ci.org/MasseGuillaume/ScalaKata) [![Stories in Ready](https://badge.waffle.io/MasseGuillaume/ScalaKata.png?label=Ready)](https://waffle.io/MasseGuillaume/ScalaKata)

![scala kata instructor mode](https://raw.githubusercontent.com/MasseGuillaume/ScalaKata/develop/Examples/Instructor.png)
*Instructor Mode*

## Distributions

### Sbt Plugin

> ### 1. add the plugin to project/plugins.sbt

```scala
addSbtPlugin("com.scalakata" % "plugin" % "0.9.0")
```

> ### 2. and add the settings to your build definition build.sbt

```scala
seq(kataSettings: _*)
```

> ### 3. or in your Build.scala

```scala
com.scalakata.Scalakata.kataSettings
```

> ### 4. start with ```sbt kstart```

### Docker Container

 > ### 1. It's also possible to run scala kata in a docker container:

```
sudo docker run -p 7331:7331 --name scalakata masseguillaume/scalakata:0.9.0
```

 > ### 2. open your browser at http://localhost:7331
