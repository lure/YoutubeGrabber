package ru.shubert.yt

import javax.script.{Invocable, ScriptEngineManager}
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.matching.{Regex, UnanchoredRegex}
import org.slf4j.LoggerFactory


/**
  * Extracts and caches decode function from YouTube html5 player.
  * Uses JavaScript Engine to execute decoding function. It may be improved by caching desipher results and so on.
  */
trait Decipher {
  import ru.shubert.yt.Decipher._
  protected val map: TrieMap[String, DecipherFunction] = TrieMap[String, DecipherFunction]()
  // dirty hack, read constructor declaration carefully
  protected lazy val factory = new ScriptEngineManager(null)
  protected implicit def ec: ExecutionContext
  // player parsing regexps
  protected lazy val FindProcName: UnanchoredRegex = """set\("signature",\s*(?:([^(]*).*)\);""".r.unanchored
  protected lazy val ExtractSubProcName: UnanchoredRegex = """(\w*).\w+\(\w+,\s*\d+\)""".r.unanchored
  protected lazy val ExternalFuncName: String = "decipher"

  /**
    * Downloads player, attempts to find decipher function and it's requirements, wrap it with
    * custom name and store for future uses. Subsequental call with the same player url should not download it again,
    * Alternative solution is to store the whole player and call it's functions.
    *
    * @param playerUrl    where to get player
    * @param downloadFunc which function to use to download player
    * @return Invocable function
    */
  def registerPlayer(playerUrl: String, downloadFunc: (String) => Future[String]): DecipherFunction = {
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
    val finalUrl = if (playerUrl.startsWith("http")) {
      playerUrl
    } else {
      if (playerUrl.startsWith("//youtube.com/")) {
        "https:" + playerUrl
      } else {
        "https://www.youtube.com" + playerUrl
      }
    }
    finalUrl
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

  protected def extractMainFuncName(player: String): String = {
    val epl = FindProcName.findAllIn(player)
    if (epl.hasNext) {
      val procName = epl.group(1)
      logger.debug("Found main proc name: {}", procName)
      procName
    } else {
      logger.debug(unableToFindProcName)
      throw unableToFindProcNameException
    }
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
        throw unableToFindProcBodyExceptoin
      }
    }

    val cleanName = procName.replaceAll("\\$", """\\\$""")
    // this obfuscation result was used till 2017
    lazy val ExtractProc2014 = ("""(function\s""" + cleanName + """[^}]*})""").r.unanchored

    // and this one is most recent
    val ExtractProc2017= ("(" + cleanName + """\s*\=\s*function[^}]*})""").r.unanchored

    extractBody(ExtractProc2017).orElse(extractBody(ExtractProc2014)).get
  }

  /**
    * attempts to decode signature using player stored before. No attempt to download will be made.
    *
    * @param signature to decode
    * @param playerUrl player url used as a key. No attemp to register
    * @return
    */
  def decipher(playerUrl: String)(signature: String): Future[String] = {
    map.get(playerUrl)
      .map(invoke ⇒ invoke.map(engine ⇒ engine(signature))
      ).getOrElse(Future.failed(functionMissingException))
  }
}

object Decipher {
  private val logger = LoggerFactory.getLogger(classOf[Decipher])

  type DecipherFunction = Future[(String) ⇒ String]
  // just string and exception constants
  val unableToFindSubProcBody = "Unable to find sub proc body"
  val noSubProcBodyException = YGDecipherException(unableToFindSubProcBody)
  val unableToFindSubProcName = "Unable to find sub proc name"
  val noSubProcNameException = YGDecipherException(unableToFindSubProcName)
  val unableToFindProcName = "Unable to find main proc name"
  val unableToFindProcNameException = YGDecipherException(unableToFindProcName)
  val unableToFindProcBody = "Unable to find main proc body"
  val unableToFindProcBodyExceptoin = YGDecipherException("Unable to find main proc body")
  val functionMissingException = YGDecipherException("No function exists")
}