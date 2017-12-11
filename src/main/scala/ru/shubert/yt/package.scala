package ru.shubert

/**
  * User: Shubert Alexandr 
  * Date: 04.02.2017
  * Description
  */
package object yt {
  case class StreamsHolder(video: Option[String], adaptive: Option[String])

  sealed abstract class YGException(message: String) extends Exception(message)
  case class YGDecipherException(message: String) extends YGException(message)
  case class YGNetworkException(message: String) extends YGException(message)
  case class YGParseException(message: String) extends YGException(message)
}
