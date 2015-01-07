import sbt._
import Keys._
import sbtrelease._
import xerial.sbt.Sonatype._
import ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys

object ScodecMsgPackBuild extends Build {
  import Dependencies._

  private def gitHash: String = scala.util.Try(
    sys.process.Process("git rev-parse HEAD").lines_!.head
  ).getOrElse("master")

  lazy val buildSettings = Seq(
    ReleasePlugin.releaseSettings,
    sonatypeSettings
  ).flatten ++ Seq(
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.10.4", scalaVersion.value),
    resolvers += Opts.resolver.sonatypeReleases,
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
    ),
    ReleasePlugin.ReleaseKeys.releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      ReleaseStep(
        action = state => Project.extract(state).runTask(PgpKeys.publishSigned, state)._1,
        enableCrossBuild = true
      ),
      setNextVersion,
      commitNextVersion,
      pushChanges
    ),
    credentials ++= PartialFunction.condOpt(sys.env.get("SONATYPE_USER") -> sys.env.get("SONATYPE_PASS")){
      case (Some(user), Some(pass)) =>
        Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
    }.toList,
    organization := "com.github.pocketberserker",
    homepage := Some(url("https://github.com/pocketberserker/scodec-msgpack")),
    licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
    pomExtra :=
      <developers>
        <developer>
          <id>pocketberserker</id>
          <name>Yuki Nakayama</name>
          <url>https://github.com/pocketberserker</url>
        </developer>
      </developers>
      <scm>
        <url>git@github.com:pocketberserker/scodec-msgpack.git</url>
        <connection>scm:git:git@github.com:pocketberserker/scodec-msgpack.git</connection>
        <tag>{if(isSnapshot.value) gitHash else { "v" + version.value }}</tag>
      </scm>
    ,
    description := "yet another msgpack implementation",
    pomPostProcess := { node =>
      import scala.xml._
      import scala.xml.transform._
      def stripIf(f: Node => Boolean) = new RewriteRule {
        override def transform(n: Node) =
          if (f(n)) NodeSeq.Empty else n
      }
      val stripTestScope = stripIf { n => n.label == "dependency" && (n \ "scope").text == "test" }
      new RuleTransformer(stripTestScope).transform(node)(0)
    }
  )

  lazy val msgpack = Project(
    id = "scodec-msgpack",
    base = file("."),
    settings = buildSettings
  )

  object Dependencies {

    val scodec = "org.typelevel" %% "scodec-core" % "1.6.0"
    val scalatest = "org.scalatest" %% "scalatest" % "2.2.2"
    val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.1"
    val msgpackJava = "org.msgpack" % "msgpack-core" % "0.7.0-p2"
  }
}
