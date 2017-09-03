package ru.shubert.yt

import javax.script.{Invocable, ScriptEngineManager}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{Await, Future}
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration.Duration


/**
  * Extracts and caches decode function from YouTube html5 player.
  * Uses JavaScript Engine to execute decoding function. It may be improved by caching desipher results and so on.
  */
object Decipher extends Loggable {
  private val FindProcName = """set\("signature",\s*(?:([^(]*).*)\);""".r.unanchored
  private val ExtractSubProcName = """(\w*).\w+\(\w+,\s*\d+\)""".r.unanchored
  private val ExternalFuncName = "decipher"
  private val map = TrieMap[String, DecipherFunction]()
  private lazy val factory = new ScriptEngineManager()
  type DecipherFunction = Try[(String) ⇒ Try[String]]

  /**
    * Downloads player, attempts to find decipher function and it's requirements, wrap it with
    * custom name and store for future uses. Subsequental call with the same player url should not download it again,
    * Alternative solution is to store the whole player and call it's functions.
    *
    * @param playerUrl    where to get player
    * @param downloadFunc which function to use to download player
    * @return invocable function
    */
  def registerPlayer(playerUrl: String, downloadFunc: (String) => Future[String]): DecipherFunction = {

    def buildDecipher(t: String): Try[Invocable] = {
      val finalUrl: String = calculatePlayerUrl(playerUrl)
      val playerCall = downloadFunc(finalUrl)
      Await.ready(playerCall, Duration.Inf)
      playerCall.value
      for {
        player <- playerCall.value.get
        func ← buildDecipherFunc(player)
      } yield func
    }


    map.get(playerUrl) match {
      case Some(invoker) ⇒ invoker
      case None ⇒
        val invoker = buildDecipher(playerUrl)
        val decipherFunction = invoker.flatMap(func ⇒ Try((sig: String) ⇒ Try(func.invokeFunction(ExternalFuncName, sig).toString)))
        map.put(playerUrl, decipherFunction)
        decipherFunction
    }
  }

  private def buildDecipherFunc(player: String): Try[Invocable] = {
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


  val Unable_to_find_sub_proc_body = "Unable to find sub proc body"
  val Unable_to_find_sub_proc_name = "Unable to find sub proc name"

  private def extractSubProc(player: String, mainProcBody: String): Try[String] = {
    // decoding sub proc
    val sbNameRE = ExtractSubProcName.findAllIn(mainProcBody)
    (if (sbNameRE.hasNext) {
      val subProcName = sbNameRE.group(1)
      LOG.debug("Found sub proc name: {}", subProcName)
      Success(subProcName)
    } else {
      LOG.debug(Unable_to_find_sub_proc_name)
      Failure(new YGDecipherException(Unable_to_find_sub_proc_name))
    }).flatMap { subProcName ⇒
      val sbBodyRE = ("(?U)(var " + subProcName + """=\{.*?(?=\};))""").r.unanchored
      val sb = sbBodyRE.findAllIn(player)
      if (sb.hasNext) {
        val sbBody = sb.group(1)
        LOG.debug("Found sub proc body: {}", sbBody)
        Success(sbBody)
      } else {
        LOG.debug(Unable_to_find_sub_proc_body)
        Failure(new YGDecipherException(Unable_to_find_sub_proc_body))
      }
    }
  }

  private def extractMainFuncName(player: String): Try[String] = Try {
    val epl = FindProcName.findAllIn(player)
    if (epl.hasNext) {
      val procName = epl.group(1)
      LOG.debug("Found main proc name: {}", procName)
      procName
    } else {
      LOG.debug("Unable to find main proc name")
      throw new YGDecipherException("Unable to find main proc name")
    }
  }

  private def extractMainFuncBody(player: String, procName: String): Try[String] = {
    def extractBody(regex: Regex): Try[String] = Try {
      val proc = regex.findAllIn(player)
      if (proc.hasNext) {
        val b = proc.group(1)
        LOG.debug("Found main proc body: {}", b)
        b
      } else {
        LOG.debug("Unable to find main proc body")
        throw new YGDecipherException("Unable to find main proc body")
      }
    }

    // this obfuscation result was used till 2017
    def ExtractProc2014(procName: String) = ("""(function\s""" + procName + """[^}]*})""").r.unanchored

    // and this one is most recent
    def ExtractProc2017(procName: String) = ("(" + procName + """\s*\=\s*function[^}]*})""").r.unanchored

    extractBody(ExtractProc2017(procName)).orElse(extractBody(ExtractProc2014(procName)))
  }

  /**
    * attempts to decode signature using player stored before. No attempt to download will be made.
    *
    * @param signature to decode
    * @param playerUrl player url used as a key. No attemp to register
    * @return
    */
  def decipher(playerUrl: String)(signature: String): Try[String] = {
    map.get(playerUrl)
      .map(invoke ⇒ invoke.flatMap(engine ⇒ engine(signature))
      ).getOrElse(Failure(new YGDecipherException("No function exists")))
  }
}
