#Upgrading Play

* to Play version 2.4.2

* [Migration to 2.3](https://www.playframework.com/documentation/2.3.x/Migration23)
  - in  ``project/plugins.sbt`` changed ``sbt-plugin`` version to 2.3.0
  - in ``project/build.properties`` changed ``sbt.version`` to 0.13.5
  - in ``build.sbt``
    -  added line ``scalaVersion := "2.11.1"``
    - replaced  
      ``play.Project.playScalaSettings``  
      with  
      ``lazy val root = (project in file(".")).enablePlugins(PlayScala)``
    - set ``org.scalatestplus`` to 1.1.0

* [Migration to 2.4](https://www.playframework.com/documentation/2.4.x/Migration24)
  - in ``project/build.properties`` changed ``sbt.version`` to 0.13.8
  - in  ``project/plugins.sbt``
    - changed ``sbt-plugin`` version to 2.4.2
    - added ``addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")`` to run ``activator eclipse``
    - added ``resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"`` (needed for Specs2)
  - in ``build.sbt``
    - replaced ``anorm`` with ``"com.typesafe.play" %% "anorm" % "2.4.0"``
    - added ``"net.databinder.dispatch" %% "dispatch-core" % "0.11.2"``
    - added ``specs2 % Test``

* still problems with dependencies:
  - needed to add ``"io.netty" % "netty" % "3.9.2.Final" force()`` to run the app
  - cannot run Specs2 atm

## List of libraries

* **[anorm](https://www.playframework.com/documentation/2.4.x/Anorm)** @latest 2.4.0
* **[Casbah](https://mongodb.github.io/casbah/guide/installation.html)** @latest 2.8.1
* **[dispatch](http://dispatch.databinder.net/Dispatch.html)** @latest 0.11.2
* **[salat](https://github.com/novus/salat)** @latest 1.9.9 (serialisation library used in Casbah)
* **[sbteclipse](https://github.com/typesafehub/sbteclipse)** @latest 4.0.0
  * [Setting up your preferred IDE](https://www.playframework.com/documentation/2.4.x/IDE)
* **[scalatestplus](http://scalatest.org/plus/play/versions)** @latest 1.4.0-M3
* **[sqlite-jdbc](https://bitbucket.org/xerial/sqlite-jdbc/overview)** @latest 3.8.10.1
