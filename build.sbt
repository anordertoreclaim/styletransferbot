name := "project"

version := "0.1"

scalaVersion := "2.12.8"

resolvers += Opts.resolver.sonatypeReleases

libraryDependencies += "org.platanios" %% "tensorflow" % "0.2.4" classifier "darwin-cpu-x86_64"
libraryDependencies += "com.bot4s" %% "telegram-core" % "4.4.0-RC2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % Test
libraryDependencies += "com.typesafe" % "config" % "1.4.0"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"