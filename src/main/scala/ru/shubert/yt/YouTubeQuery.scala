package ru.shubert.yt

import _root_.java.io.{BufferedReader, InputStreamReader}
import _root_.java.net.URLDecoder
import _root_.java.nio.charset.StandardCharsets
import java.security.MessageDigest

import scala.collection.JavaConverters._
import cats.MonadError
import cats.implicits._
import com.typesafe.scalalogging.Logger
import _root_.org.apache.http.NameValuePair
import _root_.org.apache.http.message.BasicNameValuePair
import io.circe.HCursor

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.client.utils.{HttpClientUtils, URLEncodedUtils}
import org.apache.http.impl.client.HttpClients

import scala.collection.mutable
import scala.util.matching.UnanchoredRegex
import scala.util.{Failure, Success, Try}
import scala.language.higherKinds

import io.circe._, io.circe.parser._

/**
  * YouTube obscures download links, requiring urls with special signature in it.
  *
  * just 4k video  http://www.youtube.com/watch?v=Cx6eaVeYXOs
  *
  * prefix `s=`  https://www.youtube.com/watch?v=UxxajLWwzqY | url_encoded_fmt_stream_map
  * normal `s=`  https://www.youtube.com/watch?v=UxxajLWwzqY | adaptive_fmts
  * normal `s=`  https://www.youtube.com/watch?v=8UVNT4wvIGY | url_encoded_fmt_stream_map
  */
class YouTubeQuery[F[_]](implicit M: MonadError[F, Throwable]) extends SignatureDecipher[F] {

  import YouTubeQuery._

  protected[yt] val ReqConfig: RequestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000).setConnectTimeout(5000).setRedirectsEnabled(true).build()

  //noinspection ConvertExpressionToSAM
  //  implicit val ordering: Ordering[NameValuePair] = (x: NameValuePair, y: NameValuePair) => x.getName.compare(y.getName)
  protected implicit val ordering: Ordering[NameValuePair] = new Ordering[NameValuePair] {
    override def compare(x: NameValuePair, y: NameValuePair): Int = x.getName.compare(y.getName)
  }

  case class SingleStream(urlExploded: Array[String], params: mutable.Buffer[NameValuePair])

  case class TagStream(itag: Try[String] = defaultITag,
                       signature: Try[String] = defaultSignature,
                       params: mutable.TreeSet[NameValuePair] = mutable.TreeSet[NameValuePair]())

  protected[yt] def readStringFromUrl(url: String): F[String] = {
    M.catchNonFatal {
      val method = new HttpGet(url)
      method.addHeader("Accept-Charset", StandardCharsets.UTF_8.name())
      method.addHeader("User-Agent", ModernBrowser)
      method.setConfig(ReqConfig)

      val client = HttpClients.createDefault()
      var resp: CloseableHttpResponse = null

      try {
        resp = client.execute(method)
        val status = resp.getStatusLine.getStatusCode
        if (status == 200) {
          logger.debug("Successful download for url {}", url)
          val stream = new BufferedReader(new InputStreamReader(resp.getEntity.getContent))
          val buffer = new StringBuilder
          Iterator.continually(stream.readLine()).takeWhile(_ != null).foreach(buffer.append)
          buffer.toString()
        } else {
          val msg = s"Error code $status while accessing $url"
          logger.debug(msg)
          throw YGNetworkException(msg)
        }
      } finally {
        HttpClientUtils.closeQuietly(client)
        HttpClientUtils.closeQuietly(resp)
      }
    }
  }

  protected def MD5(value: String): String = {
    val md5 = MessageDigest.getInstance("MD5")
    md5.update(value.getBytes(StandardCharsets.UTF_8))
    md5.digest().take(5).map("%02x".format(_)).mkString
  }

  /**
    * Extracts player config from a quite long javascript string.
    *
    * @param page where player should be found
    * @return json nodes wrapped in Success or Failure with exception
    */
  protected def getPlayerConfig(page: String): F[HCursor] = page match {
    case PlayerConfigRegex(streams) =>
      M.pure(parse(streams).getOrElse(Json.Null).hcursor)
    case _ =>
      logger.error("Unable to extract player config from (first 300) " + page.take(300))
      M.raiseError(YGParseException("Player script was changed: " + page))
  }

  // Extract video+audio streams and converts from escaped to plain
  protected def extractStreamsUrl(cfg: HCursor): F[StreamsHolder] = {
    //TODO: lost exception
    import cats.implicits._
    def extract(name: String, doc: ACursor): Option[String] = {
      doc.get[String](name).bimap(
        e => {
          logger.error(s"Failed to extract $name", e)
          e
        },
        StringContext.treatEscapes)
        .toOption
    }

    val root = cfg.downField("args")
    val vf = M.catchNonFatal(extract("url_encoded_fmt_stream_map", root))
    val af = M.catchNonFatal(extract("adaptive_fmts", root))

    for {
      video ← vf
      adaptive ← af
    } yield {
      logger.debug("Video streams?: {} \n adaptive? {}", video.isDefined, adaptive.isDefined)
      StreamsHolder(video, adaptive)
    }
  }

  protected def getPlayerUrl(cfg: HCursor): F[String] =
    M.fromEither(cfg.downField("assets")
      .get[String]("js")
      .map(URLDecoder.decode(_, StandardCharsets.UTF_8.name())))


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
    * @param urls     block of urls
    * @param decipher decipher function
    * @return Map of videoType to url relations
    */
  protected def buildDownloadLinks(urls: String, decipher: String ⇒ String): Seq[(Int, String)] = {
    val md5 = MD5(urls)

    def getSingleStream(desc: String) = {
      // Why so complicate? Youtube servers rejects requests with : 1.duplicate tags (!!!), 2.with + replaced with ' ', 3.on some urldecodings.
      // So here we doing our best not to interfere with params.
      val params1 = URLEncodedUtils.parse(desc, StandardCharsets.UTF_8).asScala
      val splitUrl = params1.partition(_.getName == "url")
      val urlExploded = splitUrl._1.head.getValue.split("\\?")
      val decodedLine = URLDecoder.decode(urlExploded(1), StandardCharsets.UTF_8.name())
      val params = splitUrl._2 ++ URLEncodedUtils.parse(decodedLine, StandardCharsets.UTF_8).asScala
      SingleStream(urlExploded, params)
    }

    urls.split(",") flatMap { desc =>
      logger.debug(s"For $md5 parsed url $desc")
      val singleStream: SingleStream = getSingleStream(desc)
      val urlExploded: Array[String] = singleStream.urlExploded
      val taggedStream = singleStream.params.foldLeft(TagStream()) { case (acc, pair) ⇒
        pair.getName match {
          case "signature" ⇒ acc.copy(signature = Success(pair.getValue))
          case "s" | "sign" ⇒ acc.copy(signature = Try(decipher(pair.getValue)))
          case "itag" ⇒ acc.copy(itag = Success(pair.getValue))
          case _ ⇒ // since 2016 youtube denies urls with empty params.
            if (pair.getValue != null && !pair.getValue.trim.isEmpty) {
              acc.params.add(pair)
            }
            acc
        }
      }

      logger.debug(s"For $md5 params are ${taggedStream.params}")
      (for {
        tag ← taggedStream.itag
        sig ← taggedStream.signature
      } yield {
        taggedStream.params.add(new BasicNameValuePair("signature", sig))
        taggedStream.params.add(new BasicNameValuePair("itag", tag))
        val link = urlExploded(0) + "?" + URLEncodedUtils.format(taggedStream.params.toList.asJava, StandardCharsets.UTF_8)
        tag.toInt -> link
      }).toOption
    }
  }

  /**
    * Parse streams from whole page represented by string.
    *
    * @param page youtube video html page
    * @return optional map of streams
    */
  def getStreamsFromString(page: String): F[Map[Int, String]] = {
    for {
      cfg <- getPlayerConfig(page)

      streamsF = extractStreamsUrl(cfg)
      playerUrlF = getPlayerUrl(cfg)

      streams ← streamsF
      playerUlr ← playerUrlF
      decipher ← registerPlayer(playerUlr, readStringFromUrl)

      videoF = M.catchNonFatal(streams.video.map(buildDownloadLinks(_, decipher)))
      adaptiveF = M.catchNonFatal(streams.adaptive.map(buildDownloadLinks(_, decipher)))

      video ← videoF
      adaptive ← adaptiveF
    } yield {
      // Option[Seq[(Int, String)]]
      (video ++ adaptive)
        .flatten
        .toMap
    }
  }

  /**
    * Scala oriented method that returns possible video streams.
    *
    * @param url video url of form `https://www.youtube.com/watch?v=ecekSCX3B4Q`
    * @return option contains map of type to video url
    */
  def getStreams(url: String): F[Map[Int, String]] = readStringFromUrl(url).flatMap(getStreamsFromString)
}

object YouTubeQuery {
  private val logger = Logger(getClass.getName)
  lazy val ModernBrowser = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12) AppleWebKit/602.1.50 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36 Firefox/62.0"
  val PlayerConfigRegex: UnanchoredRegex = """(?i)ytplayer\.config\s*=\s*(\{.*\});\s*ytplayer\.load""".r.unanchored
  lazy val defaultITag = Failure(YGParseException("itag not found"))
  lazy val defaultSignature = Failure(YGParseException("subscription not found"))
  lazy val unableToExtractJsException = throw YGParseException("Failed to extract js")
}