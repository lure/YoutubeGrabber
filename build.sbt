import Dependencies._


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "ru.shubert",
      description := "Youtube video grabber",
      scalaVersion := "2.12.3",
      version := "1.3"
    )),
    name := "youtube-grabber",
    libraryDependencies ++= Seq(
      apacheCommons,
      apacheHttp,
      jackson,
      logbackcore,
      logbackclassic,
      scalatest % Test
    )
  )

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
) ++ credentialSettings

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

//lazy val credentialSettings = Seq(
//  credentials += Credentials(Path.userHome / ".sbt" / "1.0" /"sonatype.sbt")
//)
