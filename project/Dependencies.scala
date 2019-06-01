import sbt._

//noinspection TypeAnnotation
object Dependencies {
  val apacheHttp = "org.apache.httpcomponents" % "httpclient" % "4.3.3"
  val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.7"
  val cats = "org.typelevel" %% "cats-core" % "1.4.0"
  val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"


  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
}
