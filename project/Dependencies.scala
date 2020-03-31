import sbt._

//noinspection TypeAnnotation
object Dependencies {
  val apacheHttp = "org.apache.httpcomponents" % "httpclient" % "4.3.3"
  val jsonParser1 = "io.circe" %% "circe-parser" % "0.12.3"
  val jsonParser2 = "io.circe" %% "circe-generic" % "0.12.3"
  val cats = "org.typelevel" %% "cats-core" % "2.0.0"
  val scalaParallel = "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"
  val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
}
