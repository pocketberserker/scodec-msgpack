import sbt._
import Keys._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import xerial.sbt.Sonatype._
import com.typesafe.sbt.pgp.PgpKeys
import sbtbuildinfo.Plugin._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object ScodecMsgPackBuild extends Build {

  private def gitHash: String = scala.util.Try(
    sys.process.Process("git rev-parse HEAD").lines_!.head
  ).getOrElse("master")

  private val tagName = Def.setting{
    s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
  }
  private val tagOrHash = Def.setting{
    if(isSnapshot.value) gitHash else tagName.value
  }

  private[this] val unusedWarnings = (
    "-Ywarn-unused" ::
    "-Ywarn-unused-import" ::
    Nil
  )

  lazy val buildSettings = Seq(
    sonatypeSettings,
    buildInfoSettings
  ).flatten ++ Seq(
    name := "scodec-msgpack",
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.10.6", scalaVersion.value),
    scalaJSStage in Global := FastOptStage,
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
    scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
      case Some((2, v)) if v >= 11 => unusedWarnings
    }.toList.flatten,
    fullResolvers ~= {_.filterNot(_.name == "jcenter")},
    libraryDependencies ++= Seq(
      "org.scodec" %%% "scodec-core" % "1.9.0",
      "org.scalatest" %%% "scalatest" % "3.0.0-M12" % "test",
      "org.scalacheck" %%% "scalacheck" % "1.12.5" % "test"
    ),
    libraryDependencies ++= {
     if (scalaBinaryVersion.value startsWith "2.10")
       Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full))
     else
       Nil
    },
    buildInfoKeys := Seq[BuildInfoKey](
      organization,
      name,
      version,
      scalaVersion,
      sbtVersion,
      scalacOptions,
      licenses
    ),
    buildInfoPackage := "scodec.msgpack",
    buildInfoObject := "BuildInfoScodecMsgpack",
    sourceGenerators in Compile <+= buildInfo,
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishArtifact in Test := false,
    releaseTagName := tagName.value,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
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
        <tag>{tagOrHash.value}</tag>
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
  ) ++ Seq(Compile, Test).flatMap(c =>
    scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
  )

  lazy val root = project.in(file(".")).settings(
    buildSettings
  ).settings(
    publish := {},
    publishLocal := {},
    publishArtifact := false
  ).aggregate(msgpackJS, msgpackJVM)

  lazy val msgpack = crossProject.crossType(CrossType.Full).in(file(".")).settings(
    buildSettings: _*
  ).jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/pocketberserker/scodec-msgpack/" + tagOrHash.value
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  ).jvmSettings(
    libraryDependencies += "org.msgpack" % "msgpack-core" % "0.8.1" % "test"
  )

  lazy val msgpackJVM = msgpack.jvm
  lazy val msgpackJS = msgpack.js

}
