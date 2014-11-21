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
      scalacOptions ++= (
        "-deprecation" ::
        "-unchecked" ::
        "-Xlint" ::
        "-language:existentials" ::
        "-language:higherKinds" ::
        "-language:implicitConversions" ::
        Nil
      ),
      scalacOptions ++= {
        if(scalaVersion.value.startsWith("2.11"))
          Seq("-Ywarn-unused", "-Ywarn-unused-import")
        else
          Nil
      },
      libraryDependencies ++= Seq(
        scodec,
        scalatest % "test",
        msgpackJava % "test",
        scalacheck % "test"
      )
    )

  lazy val msgpack = Project(
    id = "scodec-msgpack",
    base = file("."),
    settings = buildSettings
  )

  object Dependencies {

    val scodec = "org.typelevel" %% "scodec-core" % "1.5.0"
    val scalatest = "org.scalatest" %% "scalatest" % "2.2.2"
    val scalacheck = "org.scalacheck" %% "scalacheck" % "1.11.6"
    val msgpackJava = "org.msgpack" % "msgpack-core" % "0.7.0-M5"
  }
}
