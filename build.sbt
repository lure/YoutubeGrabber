import Dependencies._

val mainScala = "2.13.1"

lazy val root = (project in file("."))
  .settings(
    List(
      name := "youtubegrabber",
      organization := "ru.shubert",
      description := "Youtube video grabber",
      scalaVersion := mainScala,
      crossScalaVersions := Seq("2.11.8", "2.12.11", mainScala),
      isSnapshot := false
    ),
    libraryDependencies ++= Seq(
      apacheHttp,
      jsonParser1,
      jsonParser2,
      cats,
      catsEffect,
      scalaParallel,
      logging,
      logback,
      scalaTest,

    )
  )
  .settings(assemblySetting)
  .settings(publishSettings)

lazy val assemblySetting = Seq(
  test in assembly := {},
  mainClass in assembly := Some("Main"),
  assemblyOutputPath in assembly :=
    file(baseDirectory.value.getAbsolutePath + s"/docker/grabber.jar")
//    file(baseDirectory.value.getAbsolutePath + s"/docker/grabber-${version.value}.jar")
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },

  homepage := Some(url("https://github.com/lure/YoutubeGrabber")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(ScmInfo(url("https://github.com/lure/YoutubeGrabber"), "scm:git:git@github.com:lure/YoutubeGrabber.git")),
  organization := "ru.shubert",
  developers := List(
    Developer(
      id    = "lure",
      name  = "Alexandr Shubert",
      email = "alex.shubert@gmail.com",
      url   = url("https://github.com/lure")
    )
  ),
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