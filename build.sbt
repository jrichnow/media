name := "media"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  specs2 % Test,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "io.netty" % "netty" % "3.9.2.Final" force(),
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.mongodb" %% "casbah" % "2.8.1",
  "com.novus" %% "salat" % "1.9.9",
  "org.xerial" % "sqlite-jdbc" % "3.8.10.1",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)