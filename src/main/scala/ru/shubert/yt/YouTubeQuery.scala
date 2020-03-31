package ru.shubert.yt

import _root_.java.io.{BufferedReader, InputStreamReader}
import _root_.java.net.URLDecoder
import _root_.java.nio.charset.StandardCharsets
import java.security.MessageDigest

import cats.MonadError
import cats.implicits._
import com.typesafe.scalalogging.Logger
import io.circe.{HCursor, _}
import io.circe.parser._
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.impl.client.HttpClients
import ru.shubert.yt.SignatureDecipher.NotKnownYet
import ru.shubert.yt.YGException.{YGNetworkException, YGParseException}
import scala.util.matching.UnanchoredRegex

/**
  * YouTube obscures download links, requiring urls with special signature in it.
  *
  * just 4k video  http://www.youtube.com/watch?v=Cx6eaVeYXOs
  *
  * prefix `s=`  https://www.youtube.com/watch?v=UxxajLWwzqY | url_encoded_fmt_stream_map
  * normal `s=`  https://www.youtube.com/watch?v=UxxajLWwzqY | adaptive_fmts
  * normal `s=`  https://www.youtube.com/watch?v=8UVNT4wvIGY | url_encoded_fmt_stream_map
  */
class YouTubeQuery[F[_]](implicit M: MonadError[F, Throwable]) extends StreamParser with SignatureDecipher {
  import YouTubeQuery._

  protected[yt] def readStringFromUrl(url: String): F[String] = {
    M.catchNonFatal {
      val method = new HttpGet(url)
      method.setHeader("Accept-Charset", StandardCharsets.UTF_8.name())
      method.setHeader("User-Agent", ModernBrowser)
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


  protected def getPlayerUrl(jsPlayerParams: HCursor): Either[DecodingFailure, String] =
    jsPlayerParams.downField("assets")
      .get[String]("js")
      .map(URLDecoder.decode(_, StandardCharsets.UTF_8.name()))


  /**
    * Parse streams from whole page represented by string.
    *
    * @param page youtube video html page
    * @return optional map of streams
    */
  def getStreamsFromString(page: String, videoOnly: Boolean): F[Map[Int, Format]] = {
    for {
      cfg <- getPlayerConfig(page)
      playerUlr <- M.fromEither(getPlayerUrl(cfg))
      streams <- M.fromEither(extractStreamsUrl(cfg))
      decipher <- getDecipher(playerUlr) match {
        case Right(decipher) =>
          M.pure(decipher)
        case Left(NotKnownYet(pl)) =>
          readStringFromUrl(pl.v).flatMap(body => M.fromEither(registerPlayer(pl, body)))
        case Left(e) =>
          M.raiseError[String => String](e)
      }

      video <- M.fromEither(buildDownloadLinks(streams.video, decipher))
      adaptive <- M.fromEither( if (!videoOnly)
                    buildDownloadLinks(streams.adaptive, decipher)
                  else
                    List[(Int, Format)]().asRight
      )
    } yield {
      //TODO: do not like, return formats and adaptives?
      (video ++ adaptive).toMap
    }
  }

  /**
    * Scala oriented method that returns possible video streams.
    *
    * @param url video url of form `https://www.youtube.com/watch?v=ecekSCX3B4Q`
    * @return option contains map of type to video url
    */
  def getStreams(url: String, videoOnly: Boolean = false): F[Map[Int, Format]] =
    readStringFromUrl(url).flatMap(getStreamsFromString(_, videoOnly))
}

object YouTubeQuery {
  private val logger = Logger(getClass.getName)

  protected[yt] val ReqConfig: RequestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000)
    .setConnectTimeout(5000).setRedirectsEnabled(true).build()
  lazy val ModernBrowser = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Safari/605.1.15 Chrome/80.0.3987.149 Firefox/74.0"

  val PlayerConfigRegex: UnanchoredRegex = """(?i)ytplayer\.config\s*=\s*(\{.*\});\s*ytplayer\.load""".r.unanchored
  lazy val unableToExtractJsException = throw YGParseException("Failed to extract js")
  final case class Format(itag: Int,
                    mimeType: String,
                    bitrate: Int,
                    width: Option[Int],
                    height: Option[Int],
                    contentLength: Option[String],
                    quality:Option[String],
                    qualityLabel:Option[String],
                    projectionType:String,
                    averageBitrate:Option[Int],
                    audioQuality:Option[String],
                    approxDurationMs: String,
                    audioSampleRate: Option[String],
                    audioChannels: Option[Int],
                    cipher: Option[String],
                    url: Option[String])
}