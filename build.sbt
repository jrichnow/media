name := "media"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "org.mongodb" %% "casbah" % "2.7.2",
  "com.novus" %% "salat" % "1.9.8",
  "org.xerial" % "sqlite-jdbc" % "3.7.15-M1",
  "org.scalatestplus" %% "play" % "1.1.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)