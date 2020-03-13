package ru.shubert.yt

import javax.script.{Invocable, ScriptEngineManager}

import scala.collection.concurrent.TrieMap
import scala.util.Try
import scala.util.matching.{Regex, UnanchoredRegex}
import cats.MonadError
import cats.implicits._
import com.typesafe.scalalogging.Logger

import scala.language.higherKinds

/**
  * Extracts and caches decode function from YouTube html5 player.
  * Uses JavaScript Engine to execute decoding function. It may be improved by caching desipher results and so on.
  */
class SignatureDecipher[F[_]](implicit M: MonadError[F, Throwable]) {
  import SignatureDecipher._

  type DecipherFunction = F[String ⇒ String]

  protected val map: TrieMap[String, DecipherFunction] = TrieMap[String, DecipherFunction]()

  /**
    * Downloads player, attempts to find decipher function and it's requirements, wrap it with
    * custom name and store for future uses. Subsequental call with the same player url should not download it again,
    * Alternative solution is to store the whole player and call it's functions.
    *
    * @param playerUrl    where to get player
    * @param downloadFunc which function to use to download player
    * @return Invocable function
    */
  def registerPlayer(playerUrl: String, downloadFunc: String => F[String]): DecipherFunction = {
    import cats.syntax.all._
    map.getOrElse(playerUrl, {
      val finalUrl: String = calculatePlayerUrl(playerUrl)
      val invoker = downloadFunc(finalUrl).map(buildDecipherFunc)
      val decipherFunction = invoker.map(func ⇒ (sig: String) ⇒ func.invokeFunction(ExternalFuncName, sig).toString)
      map.put(playerUrl, decipherFunction)
      decipherFunction
    })
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
  protected def calculatePlayerUrl(playerUrl: String): String = {
    if (playerUrl.startsWith("http")) {
      playerUrl
    } else {
      if (playerUrl.startsWith("//youtube.com/")) {
        "https:" + playerUrl
      } else {
        "https://www.youtube.com" + playerUrl
      }
    }
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
        throw noSubProcBodyException
      }
    } else {
      logger.debug(unableToFindSubProcName)
      throw noSubProcNameException
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
        throw unableToFindProcNameException
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
        throw unableToFindProcBodyException
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
  def decipher(playerUrl: String)(signature: String): F[String] = {
    map.get(playerUrl)
      .map(invoke ⇒ invoke.map(engine ⇒ engine(signature))
      ).getOrElse(M.raiseError(functionMissingException))
  }
}

object SignatureDecipher {

  private val logger = Logger(getClass)
  // dirty hack, read constructor declaration carefully
  protected lazy val factory = new ScriptEngineManager(null)
  // player parsing regexps
  protected val FindProcName2015: UnanchoredRegex = """set\("signature",\s*(?:([^(]*).*)\);""".r.unanchored
  protected val FindProcName2018: UnanchoredRegex = """"signature"\),\s*\w*\.set[^,]+,([^(]*).*\)""".r.unanchored
  protected val FindProcName2018_2: UnanchoredRegex = """(\w+)\s*=\s*function\s*\(\w\)\s*\{\s*\w\s*=\s*\w\.split\(\"\"\);""".r.unanchored
  protected val FindProcName2020: UnanchoredRegex = """\b([a-zA-Z0-9$]{2})\s*=\s*function\(\s*a\s*\)\s*\{\s*a\s*=\s*a\.split\(\s*""\s*\)""".r.unanchored
  protected val ExtractSubProcName: UnanchoredRegex = """(\w*).\w+\(\w+,\s*\d+\)""".r.unanchored
  protected val ExternalFuncName: String = "decipher"

  // just string and exception constants
  val unableToFindSubProcBody = "Unable to find sub proc body"
  val noSubProcBodyException: YGDecipherException = YGDecipherException(unableToFindSubProcBody)
  val unableToFindSubProcName = "Unable to find sub proc name"
  val noSubProcNameException: YGDecipherException = YGDecipherException(unableToFindSubProcName)
  val unableToFindProcName = "Unable to find main proc name"
  val unableToFindProcNameException: YGDecipherException = YGDecipherException(unableToFindProcName)
  val unableToFindProcBody = "Unable to find main proc body"
  val unableToFindProcBodyException: YGDecipherException = YGDecipherException("Unable to find main proc body")
  val functionMissingException: YGDecipherException = YGDecipherException("No function exists")
}
