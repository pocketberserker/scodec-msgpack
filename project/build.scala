import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

object SCodecMsgPackBuild extends Build {
  import Dependencies._

  lazy val buildSettings = Defaults.defaultSettings ++
    Revolver.settings ++
    Seq(
      scalaVersion := "2.11.2",
      crossScalaVersions := Seq("2.10.4", scalaVersion.value),
      resolvers += Resolver.sonatypeRepo("releases"),
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= Seq(
        scodec,
        scalatest % "test"
      )
    )

  lazy val sideburns = Project(
    id = "scodec-msgpack",
    base = file("."),
    settings = buildSettings
  )

  object Dependencies {

    val scodec = "org.typelevel" %% "scodec-core" % "1.2.0"
    val scalatest = "org.scalatest" %% "scalatest" % "2.2.1"
  }
}
