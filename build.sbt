import Dependencies._


lazy val root = (project in file("."))
  .settings(
    List(
      name := "youtubegrabber",
      organization := "ru.shubert",
      description := "Youtube video grabber",
      scalaVersion := "2.12.3",
      crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.3"), //"2.13.0-M2" fails, see https://github.com/sbt/sbt/issues/3427
      version := "1.4",
      isSnapshot := false
    ),
    libraryDependencies ++= Seq(
      apacheCommons,
      apacheHttp,
      jackson,
      logbackcore,
      logbackclassic,
      scalatest % Test
    )
  )
  .settings(publishSettings)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },

  homepage := Some(url("https://github.com/lure/YoutubeGrabber")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(ScmInfo(url("https://github.com/lure/YoutubeGrabber"), "scm:git:git@github.com:lure/YoutubeGrabber.git")),
  organization := "ru.shubert",
  pomExtra := <developers>
    <developer>
      <id>lure</id>
      <name>Alexandr Shubert</name>
      <url>https://github.com/lure/</url>
    </developer>
  </developers>,
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  )
)

import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

//lazy val noPublishSettings = Seq(
//  publish := (),
//  publishLocal := (),
//  publishArtifact := false
//)