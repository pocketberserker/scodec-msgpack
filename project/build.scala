import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

object ScodecMsgPackBuild extends Build {
  import Dependencies._

  lazy val buildSettings = Defaults.defaultSettings ++
    Revolver.settings ++
    Seq(
      scalaVersion := "2.11.4",
      crossScalaVersions := Seq("2.10.4", scalaVersion.value),
      resolvers += Resolver.sonatypeRepo("releases"),
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= Seq(
        scodec,
        scalatest % "test",
        scalacheck % "test"
      )
    )

  lazy val msgpack = Project(
    id = "scodec-msgpack",
    base = file("."),
    settings = buildSettings
  )

  object Dependencies {

    val scodec = "org.typelevel" %% "scodec-core" % "1.3.1"
    val scalatest = "org.scalatest" %% "scalatest" % "2.2.2"
    val scalacheck = "org.scalacheck" %% "scalacheck" % "1.11.6"
  }
}
