import sbt._

//noinspection TypeAnnotation
object Dependencies {
  val logbackVersion = "1.2.3"

  val apacheCommons = "org.apache.commons" % "commons-lang3" % "3.4"
  val apacheHttp = "org.apache.httpcomponents" % "httpclient" % "4.3.3"
  val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.1"
  val logbackcore    = "ch.qos.logback" % "logback-core"     % logbackVersion
  val logbackclassic = "ch.qos.logback" % "logback-classic"  % logbackVersion
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.4"
}
