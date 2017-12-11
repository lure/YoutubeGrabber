import sbt._

//noinspection TypeAnnotation
object Dependencies {
  val logbackVersion = "1.2.3"

  lazy val apacheCommons = "org.apache.commons" % "commons-lang3" % "3.4"
  lazy val apacheHttp = "org.apache.httpcomponents" % "httpclient" % "4.3.3"
  lazy val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.1"
  lazy val logbackClassic = "ch.qos.logback" % "logback-classic"  % logbackVersion
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4"
}
