package ru.shubert.yt

import _root_.java.io.{BufferedReader, InputStreamReader}
import _root_.java.net.URLDecoder
import _root_.java.nio.charset.StandardCharsets
import _root_.java.util.concurrent.ConcurrentHashMap
import java.math.BigInteger
import java.security.MessageDigest
import javax.script.{Invocable, ScriptEngineManager}

import _root_.org.apache.http.NameValuePair
import _root_.org.apache.http.message.BasicNameValuePair
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.client.utils.{HttpClientUtils, URLEncodedUtils}
import org.apache.http.impl.client.HttpClients

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
 * Extracts and caches decode function from YouTube html5 player.
 * Uses JavaScript Engine to execute decoding function. It may be improved by caching desipher results and so on.
 */
private[yt] object Decipher extends Loggable {
  private val FindProcName = """set\("signature",\s*(?:([^(]*).*)\);""".r.unanchored
  private val ExtractSubProcName = """(\w*).\w+\(\w+,\s*\d+\)""".r.unanchored
  private val ExternalFuncName = "decipher"
  private val map = new ConcurrentHashMap[String, Invocable]()
  val factory = new ScriptEngineManager()

  /**
   * Downloads player, attempts to find decipher function and it's requirements, wrap it with
   * custom name and store for future uses. Subsequental call with the same player url should not download it again,
   * Alternative solution is to store the whole player and call it's functions.
   *
   * @param playerUrl where to get player
   * @param downloadFunc which function to use to download player
   * @return invocable function
   */
  def registerPlayer(playerUrl: String, downloadFunc: (String) => Try[String]) = {
    if (!map.contains(playerUrl)) {
      downloadFunc("https:" + playerUrl) match {
        case Success(player) =>
          Try {
            val epl = FindProcName.findAllIn(player)
            epl.hasNext
            val procName = epl.group(1)
            LOG.debug("Found main proc name: {}", procName)

            val ExtractProc = ("""(function\s""" + procName + """[^}]*})""").r.unanchored
            val proc = ExtractProc.findAllIn(player)
            proc.hasNext
            val procBody = proc.group(1)
            LOG.debug("Found main proc body: {}", procBody)

            // decoding sub proc
            val sbNameRE = ExtractSubProcName.findAllIn(procBody)
            sbNameRE.hasNext
            val subProcName = sbNameRE.group(1)
            LOG.debug("Found sub proc name: {}", subProcName)

            val sbBodyRE = ("(?U)(var " + subProcName + """=\{.*?(?=\};))""").r.unanchored
            val sb = sbBodyRE.findAllIn(player)
            sb.hasNext
            val sbBody = sb.group(1)
            LOG.debug("Found sub proc body: {}", sbBody)

            val result = s"$sbBody}; $procBody; function $ExternalFuncName(signature){ return $procName(signature); }"
            LOG.debug("Final function: {}", result)

            val engine = factory.getEngineByName("JavaScript")
            engine.eval(result)

            map.putIfAbsent(playerUrl, engine.asInstanceOf[Invocable])
          }
        case Failure(e) =>
          LOG.error("Failed to download player by url {}", playerUrl, e)
          Failure(e)
      }
    } else {
      Success(map.get(playerUrl))
    }
  }

  private def doDecipher(signature: String, playerUrl: String): Try[String] = {
    val engine = map.get(playerUrl)
    if (null != engine) {
      Try(engine.invokeFunction(ExternalFuncName, signature).toString)
    } else {
      Failure(new IllegalArgumentException("No js function for url {}", playerUrl))
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

/**
 * YouTube obscures download links, requiring urls with special signature in it.
 *
 * just 4k video  http://www.youtube.com/watch?v=Cx6eaVeYXOs
 *
 * prefix `s=`  https://www.youtube.com/watch?v=UxxajLWwzqY | url_encoded_fmt_stream_map
 * normal `s=`  https://www.youtube.com/watch?v=UxxajLWwzqY | adaptive_fmts
 * normal `s=`  https://www.youtube.com/watch?v=8UVNT4wvIGY | url_encoded_fmt_stream_map
 */
object YouTubeQuery extends Loggable {
  private val mapper = new ObjectMapper()
  private val ModernBrowser = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.0.0 Safari/537.11 Firefox/34.0"
  private val UTF8 = "UTF-8"

  private val PlayerConfigRegex = """(?i)ytplayer\.config\s*=\s*(\{.*\});\s*ytplayer\.load""".r.unanchored
  protected[yt] val ReqConfig = RequestConfig.custom().setConnectionRequestTimeout(5000).setConnectTimeout(5000)
    .setRedirectsEnabled(true).build()

  protected[yt] def download(url: String): Try[String] = Try {
    val method = new HttpGet(url)
    method.addHeader("Accept-Charset", UTF8)
    method.addHeader("User-Agent", ModernBrowser)
    method.setConfig(ReqConfig)

    val client = HttpClients.createDefault()
    var resp: CloseableHttpResponse = null
    try {
      resp = client.execute(method)
      val status = resp.getStatusLine.getStatusCode
      if (status == 200) {
        LOG.debug("Successful download for url {}", url)
        val stream = new BufferedReader(new InputStreamReader(resp.getEntity.getContent))
        val buffer = new StringBuilder
        Iterator.continually(stream.readLine()).takeWhile(_ != null).foreach(buffer.append)
        buffer.toString()
      } else {
        val msg = s"Error code $status while accessing $url"
        LOG.debug(msg)
        throw new IllegalArgumentException(msg)
      }
    } finally {
      HttpClientUtils.closeQuietly(client)
      HttpClientUtils.closeQuietly(resp)
    }
  }

  private def MD5(value: String) = new BigInteger(1,
    MessageDigest.getInstance("MD5")
      .digest(value.getBytes(UTF8))
  ).toString

  /**
   * Extracts player config from a quite long javascript string.
   * @param page where player should be found
   * @return json nodes wrapped in Success or Failure with exception
   */
  private def getPlayerConfig(page: String): Try[JsonNode] = page match {
    case PlayerConfigRegex(streams) => Try(mapper.readTree(streams))
    case _ =>
      LOG.debug("Unable to extract plaer config from (first 300) " + page.take(300))
      Failure(new IllegalArgumentException("Player script was changed " + page))
  }

  // Extract video+audio streams and converts from escaped to plain
  private def extractStreamsUrl(cfg: JsonNode): Option[String] = {
    val root = cfg.path("args")
    for {
      video <- Option(root.path("url_encoded_fmt_stream_map").asText(null))
      adapt <- Option(root.path("adaptive_fmts").asText(null))
    } yield StringEscapeUtils.unescapeJava(video + "," + adapt)
  }

  private def getPlayerUrl(cfg: JsonNode): Option[String] =
    Option(cfg.path("assets").path("js").asText(null)) map (URLDecoder.decode(_, UTF8))

  /**
   * Each stream is described by [header][\s][url with some params]
   * Signature might be put either in header or url and might be of 3 types :
   * <ul>
   * <li>`signature` - this one is plain and need no decipher</li>
   * <li>`s` - ciphered</li>
   * <li>`sig` - ciphered</li>
   * </ul>
   *
   * Obscuring function is determined by player provided and may change not with time only but even with different videos.
   * I.e. some of videos are bound to special player revision.
   *
   * @param urls block of urls
   * @param playerUrl player url
   * @return Map of videoType to url relations
   */
  private def splitLinks(urls: String, playerUrl: String): Map[Int, String] = {
    val md5 = MD5(urls)
    val urlMap = Map.newBuilder[Int, String]
    val decipher = Decipher.decipher(playerUrl) _
    
    urls.split(",") foreach { desc =>
      LOG.debug(s"For $md5 parsed url $desc")
      import scala.collection.JavaConverters._
      // Why so complicate? Youtube servers rejects requests with : 1.duplicate tags (!!!), 2.with + replaced with ' ', 3.on some urldecodings.
      // So here we doing our best not to interfere with params.
      val params1 = URLEncodedUtils.parse(desc, StandardCharsets.UTF_8).asScala
      val splitUrl = params1.partition(_.getName == "url")
      val urlExploded = splitUrl._1.head.getValue.split("\\?")
      val params = splitUrl._2 ++ URLEncodedUtils.parse(URLDecoder.decode(urlExploded(1), UTF8), StandardCharsets.UTF_8).asScala

      var tagOption: Option[String] = None
      var signatureOption: Option[String] = None
      val pams = new mutable.TreeSet[NameValuePair]()(new Ordering[NameValuePair]{
        override def compare(x: NameValuePair, y: NameValuePair): Int = x.getName.compare(y.getName)
      })

      params.foreach { p =>
        LOG.debug("new param: ", p)
        val name = p.getName
        if (name == "signature") {
          signatureOption = Some(p.getValue)
        } else if (name == "s" || name == "sign") {
          signatureOption = decipher(p.getValue)
        } else if (name == "itag") {
          pams.add(p)
          tagOption = Some(p.getValue)
        } else {
          // since 2016 youtube denies urls with empty params.
          if (p.getValue != null && !p.getValue.trim.isEmpty) {
            pams.add(p)
          }
        }
      }

      LOG.debug(s"For $md5, url $desc params are url $pams")
      for {
        tag <- tagOption
        sig <- signatureOption
      } {
        pams.add(new BasicNameValuePair("signature", sig))
        val link = urlExploded(0) + "?" + URLEncodedUtils.format(pams.toList.asJava, UTF8)
        urlMap += (tag.toInt -> link)
      }
    }
    urlMap.result()
  }

  /**
    * Parse streams from whole page represented by string.
    *
    * @param page youtube video html page
    * @return optional map of streams
    */
  def getStreamsFromString(page: String): Option[Map[Int, String]] = {
    getPlayerConfig(page) match {
      case Success(cfg) => for {
        urls <- extractStreamsUrl(cfg)
        playerUlr <- getPlayerUrl(cfg)
      } yield {
        Decipher.registerPlayer(playerUlr, download)
        splitLinks(urls, playerUlr)
      }
      case Failure(e) =>
        LOG.error("Failed to parse player's config", e)
        None
    }
  }

  /**
    * Scala oriented method that returns possible video streams.
    *
    * @param url video url of form `https://www.youtube.com/watch?v=ecekSCX3B4Q`
    * @return option contains map of type to video url
    */
  def getStreams(url: String): Option[Map[Int, String]] = download(url) match {
    case Success(page) => getStreamsFromString(page)
    case Failure(e) =>
      LOG.error("Failed to acquire youtube streams for url " + url, e)
      None
  }

  /**
    * same as getStreams, but returns empty java map if nothing found
    *
    * @param url video url from youtube
    * @return map of type to url
    */
  def getJavaStreams(url: String): java.util.Map[Int, String] = {
    import scala.collection.JavaConverters._
    getStreams(url).getOrElse(Map()).asJava
  }
}
