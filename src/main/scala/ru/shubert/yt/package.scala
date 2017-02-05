package ru.shubert

import org.slf4j.{Logger, LoggerFactory}

/**
  * User: Shubert Alexandr 
  * Date: 04.02.2017
  * Description
  */
package object yt {
  type Tagged[U] = {type Tag = U}
  type @@[T, U] = T with Tagged[U]
  
  case class StreamsHolder(video: Option[String], adaptive: Option[String])
  trait Loggable  {
    val LOG:Logger = LoggerFactory.getLogger(this.getClass)
  }
}
