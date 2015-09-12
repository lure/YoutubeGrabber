package ru.shubert.yt

import org.slf4j.{Logger, LoggerFactory}

/**
 *
 * Author: Alexandr Shubert
 */
trait Loggable  {
  val LOG:Logger = LoggerFactory.getLogger(this.getClass)
}