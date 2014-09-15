import sbt._
import Keys._
import com.github.retronym.SbtOneJar

object BuildSettings {
  val buildOrganization = "bryan.codes"
  val buildVersion      = "0.1"
  val buildScalaVersion = "2.11.2"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion
  )
}

object Resolvers {
  val typesafeRepo = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
}

object Dependencies {
  val akkaVersion = "2.3.6"

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion

  val akkaHttp = "com.typesafe.akka" %% "akka-http-core-experimental" % "0.7"
  val akkaStream ="com.typesafe.akka" %% "akka-stream-experimental" % "0.7"

  val playJson = "com.typesafe.play" %% "play-json" % "2.3.2"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.0" % "test"
  val scalaAsync = "org.scala-lang.modules" %% "scala-async" % "0.9.2"

  val akkaDependencies = Seq(akkaActor, akkaSlf4j, akkaTestkit, akkaHttp, akkaStream)
  val miscDependencies = Seq(playJson, scalaAsync)
  val testDependencies = Seq(scalatest)
  val allDependencies = akkaDependencies ++ miscDependencies ++ testDependencies
}

object AkkaHttpStreamExample extends Build {
  import Resolvers._
  import BuildSettings._
  import Defaults._

  lazy val akkaHttpStreamExample =
    Project ("akka-http-example", file("./akka-http-example"))
      .settings ( buildSettings : _* )
      .settings ( SbtOneJar.oneJarSettings : _* )
      .settings ( resolvers ++= Seq(typesafeRepo) )
      .settings ( libraryDependencies ++= Dependencies.allDependencies )
      .settings ( scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature") )

  lazy val akkaStreamExample =
    Project ("akka-stream-example", file("./akka-stream-example"))
      .settings ( buildSettings : _* )
      .settings ( SbtOneJar.oneJarSettings : _* )
      .settings ( resolvers ++= Seq(typesafeRepo) )
      .settings ( libraryDependencies ++= Dependencies.allDependencies )
      .settings ( scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature") )
}