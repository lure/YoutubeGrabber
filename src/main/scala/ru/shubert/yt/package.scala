package ru.shubert

import org.slf4j.{Logger, LoggerFactory}

/**
  * User: Shubert Alexandr 
  * Date: 04.02.2017
  * Description
  */
package object yt {
  case class StreamsHolder(video: Option[String], adaptive: Option[String])
  trait Loggable {
    val LOG: Logger = LoggerFactory.getLogger(this.getClass)
  }

  abstract class YGException(message: String) extends Exception(message)
  class YGDecipherException(message: String) extends YGException(message)
  class YGNetworkException(message: String) extends YGException(message)
  class YGParseException(message: String) extends YGException(message)
}
