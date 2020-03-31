package ru.shubert.yt

import cats.implicits._
import com.typesafe.scalalogging.Logger
import javax.script.{Invocable, ScriptEngineManager}
import ru.shubert.yt.YGException.YGDecipherException

import scala.collection.concurrent.TrieMap
import scala.util.Try
import scala.util.matching.{Regex, UnanchoredRegex}


/**
  * Extracts and caches decode function from YouTube html5 player.
  * Uses JavaScript Engine to execute decoding function. It may be improved by caching desipher results and so on.
  */
trait SignatureDecipher {
  import SignatureDecipher._

  protected val map: TrieMap[PlayerUrl, DecipherFunction] = TrieMap[PlayerUrl, DecipherFunction]()

  /**
    * Downloads player, attempts to find decipher function and it's requirements, wrap it with
    * custom name and store for future uses. Sub-sequential call with the same player url should not download it again,
    * Alternative solution is to store the whole player and call it's functions.
    *
    * @param playerUrl  where to get player
    * @param funcBody   js player body
    * @return Invocable function
    */
  def registerPlayer(playerUrl: PlayerUrl, funcBody: String): DecipherFunction = {
    map.getOrElseUpdate(playerUrl, {
      val invoker = Either.catchOnly[YGDecipherException](buildDecipherFunc(funcBody))
      invoker.map(func => (sig: String) => func.invokeFunction(ExternalFuncName, sig).toString)
    })
  }

  def getDecipher(playerUrl: String): DecipherFunction= {
    val finalUrl = calculatePlayerUrl(playerUrl)
    map.getOrElse(finalUrl, NotKnownYet(finalUrl).asLeft)
  }

  protected def buildDecipherFunc(player: String): Invocable = {
    val procName = extractMainFuncName(player)
    val procBody = extractMainFuncBody(player, procName)
    val sbBody = extractSubProc(player, procBody)
    val result = s"$sbBody}; $procBody; function $ExternalFuncName(signature){ return $procName(signature); }"

    logger.debug("Final function: {}", result)
    val engine = factory.getEngineByName("JavaScript")
    engine.eval(result)
    engine.asInstanceOf[Invocable]
  }

  /**
    * Player url specifed in video page differs from time to time. Sometimes it is full youtube domain with or w/o protocol,
    * sometimes relative part. Here we want to build valid player download link
    *
    * @param playerUrl usually malformed url obtained from video page.
    * @return valid player download url
    */
  def calculatePlayerUrl(playerUrl: String): PlayerUrl = {
    PlayerUrl(if (playerUrl.startsWith("http")) {
      playerUrl
    } else {
      if (playerUrl.startsWith("//youtube.com/")) {
        "https:" + playerUrl
      } else {
        "https://www.youtube.com" + playerUrl
      }
    })
  }

  protected def extractSubProc(player: String, mainProcBody: String): String = {
    // decoding sub proc
    val sbNameRE = ExtractSubProcName.findAllIn(mainProcBody)
    if (sbNameRE.hasNext) {
      val subProcName = sbNameRE.group(1)
      logger.debug("Found sub proc name: {}", subProcName)

      val sbBodyRE = ("(?U)(var " + subProcName + """=\{.*?(?=\};))""").r.unanchored
      val sb = sbBodyRE.findAllIn(player)
      if (sb.hasNext) {
        val sbBody = sb.group(1)
        logger.debug("Found sub proc body: {}", sbBody)
        sbBody
      } else {
        logger.debug(unableToFindSubProcBody)
        throw NoSubProcBodyException
      }
    } else {
      logger.debug(unableToFindSubProcName)
      throw NoSubProcNameException
    }
  }

  // 17285  \{\s*a\s*=\s*a.split\(""\)      (\w+)\s*=\s*function\s*\(\w+\)\s*\{\s*\w\s*=\s*a\.split\(""\);
  protected def extractMainFuncName(player: String): String = {
    def extractBody(regex: Regex): Try[String] = Try {
      val proc = regex.findAllIn(player)
      if (proc.hasNext) {
        val procName = proc.group(1)
        logger.debug("Found main proc name: {}", procName)
        procName
      } else {
        logger.debug(unableToFindProcName)
        throw UnableToFindProcNameException
      }
    }

    extractBody(FindProcName2020)
      .orElse(extractBody(FindProcName2018_2))
      .orElse(extractBody(FindProcName2018))
      .orElse(extractBody(FindProcName2015)).get
  }

  protected def extractMainFuncBody(player: String, procName: String): String = {
    def extractBody(regex: Regex): Try[String] = Try {
      val proc = regex.findAllIn(player)
      if (proc.hasNext) {
        val b = proc.group(1)
        logger.debug("Found main proc body: {}", b)
        b
      } else {
        logger.debug(unableToFindProcBody)
        throw UnableToFindProcBodyException
      }
    }

    val cleanName = procName.replaceAll("\\$", """\\\$""")
    val ExtractProc2020= ("(?s)(" + cleanName + """\s*\=\s*function.*?\w+\.join\(\"\"\)\}\;)""").r.unanchored
    extractBody(ExtractProc2020).get
  }

  /**
    * attempts to decode signature using player stored before. No attempt to download will be made.
    *
    * @param signature to decode
    * @param playerUrl player url used as a key. No attemp to register
    * @return
    */
  def decipher(playerUrl: PlayerUrl)(signature: String): Either[YGDecipherException, String] = {
    for {
      mbFunc <- map.get(playerUrl).toRight(FunctionMissingException)
      func <- mbFunc
      res <- Either.catchNonFatal(func(signature)).leftMap(e => FunctionApplicationError(e))
    } yield res
  }
}

object SignatureDecipher {
  type DecipherFunction = Either[YGDecipherException, String => String]
  private val logger = Logger(getClass)
  // bundled engines only
  protected lazy val factory = new ScriptEngineManager(null)

  case class PlayerUrl private[SignatureDecipher](v: String) extends AnyVal

  // player parsing regexps
  protected val FindProcName2015: UnanchoredRegex = """set\("signature",\s*(?:([^(]*).*)\);""".r.unanchored
  protected val FindProcName2018: UnanchoredRegex = """"signature"\),\s*\w*\.set[^,]+,([^(]*).*\)""".r.unanchored
  protected val FindProcName2018_2: UnanchoredRegex = """(\w+)\s*=\s*function\s*\(\w\)\s*\{\s*\w\s*=\s*\w\.split\(""\);""".r.unanchored
  protected val FindProcName2020: UnanchoredRegex = """\b([a-zA-Z0-9$]{2})\s*=\s*function\(\s*a\s*\)\s*\{\s*a\s*=\s*a\.split\(\s*""\s*\)""".r.unanchored
  protected val ExtractSubProcName: UnanchoredRegex = """(\w*).\w+\(\w+,\s*\d+\)""".r.unanchored
  protected val ExternalFuncName: String = "decipher"

  // just string and exception constants
  val unableToFindSubProcName = "Unable to find sub proc name"
  val unableToFindSubProcBody = "Unable to find sub proc body"
  val unableToFindProcName = "Unable to find main proc name"
  val unableToFindProcBody = "Unable to find main proc body"
  case object NoSubProcBodyException extends YGDecipherException(unableToFindSubProcBody)
  case object NoSubProcNameException extends YGDecipherException(unableToFindSubProcName)
  case object UnableToFindProcNameException extends YGDecipherException(unableToFindProcName)
  case object UnableToFindProcBodyException extends YGDecipherException(unableToFindProcBody)
  case object FunctionMissingException extends YGDecipherException("No function exists")
  case class FunctionApplicationError(e: Throwable) extends YGDecipherException(s"Exception application failed with ${e.getMessage}")
  case class NotKnownYet(player: PlayerUrl) extends YGDecipherException(s"Player url is not known yet ${player.v}")
}