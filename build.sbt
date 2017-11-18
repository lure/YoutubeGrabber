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
  homepage := Some(url("https://github.com/lure/YoutubeGrabber/")),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(url("https://github.com/lure/YoutubeGrabber/"), "scm:git:git@github.com:lure/YoutubeGrabber.git")),
  pomExtra := <developers>
    <developer>
      <id>lure</id>
      <name>Alexandr Shubert</name>
      <url>https://github.com/lure/</url>
    </developer>
  </developers>,
  publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")
) ++ credentialSettings

lazy val credentialSettings = Seq(
  credentials += Credentials(Path.userHome / ".sonatype" / ".credentials")
)
