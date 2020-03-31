package ru.shubert.yt

sealed abstract class YGException(message: String) extends Exception(message)

object YGException {
  class YGDecipherException(message: String) extends YGException(message)
  case class YGNetworkException(message: String) extends YGException(message)
  case class YGParseException(message: String) extends YGException(message)
}