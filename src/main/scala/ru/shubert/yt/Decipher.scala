package ru.shubert.yt

import java.util.concurrent.ConcurrentHashMap
import javax.script.{Invocable, ScriptEngineManager}

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

/**
  * Extracts and caches decode function from YouTube html5 player.
  * Uses JavaScript Engine to execute decoding function. It may be improved by caching desipher results and so on.
  */
object Decipher extends Loggable {
  private val FindProcName = """set\("signature",\s*(?:([^(]*).*)\);""".r.unanchored
  private val ExtractSubProcName = """(\w*).\w+\(\w+,\s*\d+\)""".r.unanchored
  private val ExternalFuncName = "decipher"
  val map = new ConcurrentHashMap[String, Invocable]()
  private lazy val factory = new ScriptEngineManager()
  private val UnableToExtractException = new Exception("unable to extract decipher")

  /**
    * Downloads player, attempts to find decipher function and it's requirements, wrap it with
    * custom name and store for future uses. Subsequental call with the same player url should not download it again,
    * Alternative solution is to store the whole player and call it's functions.
    *
    * @param playerUrl    where to get player
    * @param downloadFunc which function to use to download player
    * @return invocable function
    */
  def registerPlayer(playerUrl: String, downloadFunc: (String) => Try[String]): Try[Invocable] = {
    val adderSupplier = new java.util.function.Function[String, Invocable]() {
      override def apply(t: String): Invocable = {
        val finalUrl: String = calculatePlayerUrl(t)
        downloadFunc(finalUrl) match {
          case Success(player) =>
            buildDecipherFunc(player).getOrElse(throw UnableToExtractException)
          case Failure(e) =>
            LOG.error("Failed to download player by url " + t, e)
            throw e
        }
      }
    }
    Try(map.computeIfAbsent(playerUrl, adderSupplier))
  }

  private def buildDecipherFunc(player: String): Option[Invocable] = {
    for {
      procName ← extractMainFuncName(player)
      procBody ← extractMainFuncBody(player, procName)
      sbBody ← extractSubProc(player, procBody)
    } yield {
      val result = s"$sbBody}; $procBody; function $ExternalFuncName(signature){ return $procName(signature); }"
      LOG.debug("Final function: {}", result)
      val engine = factory.getEngineByName("JavaScript")
      engine.eval(result)
      engine.asInstanceOf[Invocable]
    }
  }

  /**
    * Player url specifed in video page differs from time to time. Sometimes it is full youtube domain with or w/o protocol,
    * sometimes relative part. Here we want to build valid player download link
    *
    * @param playerUrl usually malformed url obtained from video page.
    * @return valid player download url
    */
  private def calculatePlayerUrl(playerUrl: String) = {
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

  private def extractSubProc(player: String, mainProcBody: String): Option[String] = {
    // decoding sub proc
    val sbNameRE = ExtractSubProcName.findAllIn(mainProcBody)
    (if (sbNameRE.hasNext) {
      val subProcName = sbNameRE.group(1)
      LOG.debug("Found sub proc name: {}", subProcName)
      Some(subProcName)
    } else {
      LOG.debug("Unable to find sub proc name")
      None
    }).flatMap { subProcName ⇒
      val sbBodyRE = ("(?U)(var " + subProcName + """=\{.*?(?=\};))""").r.unanchored
      val sb = sbBodyRE.findAllIn(player)
      if (sb.hasNext) {
        val sbBody = sb.group(1)
        LOG.debug("Found sub proc body: {}", sbBody)
        Some(sbBody)
      } else {
        LOG.debug("Unable to find sub proc body")
        None
      }
    }
  }

  private def extractMainFuncName(player: String): Option[String] = {
    val epl = FindProcName.findAllIn(player)
    if (epl.hasNext) {
      val procName = epl.group(1)
      LOG.debug("Found main proc name: {}", procName)
      Some(procName)
    } else {
      LOG.debug("Unable to find main proc name")
      None
    }
  }

  private def extractMainFuncBody(player: String, procName: String) = {
    def extractBody(regex: Regex): Option[String] = {
      val proc = regex.findAllIn(player)
      if (proc.hasNext) {
        val b = proc.group(1)
        LOG.debug("Found main proc body: {}", b)
        Some(b)
      } else {
        LOG.debug("Unable to find main proc body")
        None
      }
    }

    // this obfuscation result was used till 2017
    def ExtractProc2014(procName: String) = ("""(function\s""" + procName + """[^}]*})""").r.unanchored

    // and this one is most recent
    def ExtractProc2017(procName: String) = ("(" + procName + """\s*\=\s*function[^}]*})""").r.unanchored

    extractBody(ExtractProc2017(procName)).orElse(extractBody(ExtractProc2014(procName)))
  }


  private def doDecipher(signature: String, playerUrl: String): Try[String] = {
    val engine = map.get(playerUrl)
    if (null != engine) {
      Try(engine.invokeFunction(ExternalFuncName, signature).toString)
    } else {
      Failure(new IllegalArgumentException("No js function for url " + playerUrl))
    }
  }

  /**
    * attempts to decode signature using player stored before. No attempt to download will be made.
    *
    * @param signature to decode
    * @param playerUrl player url used as a key. No attemp to register
    * @return
    */
  def decipher(playerUrl: String)(signature: String): Option[String] = {
    doDecipher(signature, playerUrl) match {
      case Success(s) => Some(s)
      case Failure(e) => LOG.warn(s"Failed to decode $signature ", e)
        None
    }
  }
}
