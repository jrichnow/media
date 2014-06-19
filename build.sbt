name := "media"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "org.mongodb" %% "casbah" % "2.7.2",
  "com.novus" %% "salat" % "1.9.8",
  "org.scalatestplus" %% "play" % "1.0.1" % "test"
)     

play.Project.playScalaSettings
