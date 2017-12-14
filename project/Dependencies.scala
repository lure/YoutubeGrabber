import sbt._

//noinspection TypeAnnotation
object Dependencies {

  val apacheCommons = "org.apache.commons" % "commons-lang3" % "3.4"
  val apacheHttp = "org.apache.httpcomponents" % "httpclient" % "4.3.3"
  val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.1"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"

  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
}
