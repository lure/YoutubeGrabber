package ru.shubert.yt

import _root_.java.net.URLDecoder
import _root_.java.nio.charset.StandardCharsets

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.typesafe.scalalogging.Logger
import io.circe.parser._
import io.circe.{HCursor, _}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import ru.shubert.yt.SignatureDecipher.NotKnownYet
import ru.shubert.yt.YGException.{YGNetworkException, YGParseException}

import scala.io.Source
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
class YouTubeQuery[F[_]: Sync] extends StreamParser with SignatureDecipher {
  import YouTubeQuery._


  protected[yt] def readStringFromUrl(url: String): F[String] = {
    def makeMethod: HttpGet = {
      val m = new HttpGet(url)
      m.setHeader("Accept-Charset", StandardCharsets.UTF_8.name())
      m.setHeader("User-Agent", ModernBrowser)
      m.setConfig(ReqConfig)
      m
    }

    (for {
      client <- Resource.fromAutoCloseable(Sync[F].delay(HttpClients.createDefault()))
      method = makeMethod
      resp <- Resource.fromAutoCloseable(Sync[F].delay(client.execute(method)))
      status = resp.getStatusLine.getStatusCode

      src <- if (status == 200) {
        Resource.fromAutoCloseable(
          Sync[F].delay(Source.fromInputStream(resp.getEntity.getContent)(scala.io.Codec.UTF8)))
      } else {
        Resource.liftF(
          Sync[F].delay {
            val msg = s"Error code $status while accessing $url"
            logger.debug(msg)
            throw YGNetworkException(msg)
          })
      }
    } yield src).use( x => Sync[F].pure(x.getLines().mkString))
  }

  /**
    * Extracts player config from a quite long javascript string.
    *
    * @param page where player should be found
    * @return json nodes wrapped in Success or Failure with exception
    */
  protected def getPlayerConfig(page: String): Either[Exception, HCursor] = {
    val z = PlayerConfigRegex.findFirstMatchIn(page)
    for {
      streams <- PlayerConfigRegex.findFirstMatchIn(page)
        .map(_.group(1))
        .toRight(YGParseException("Unable to extract player config"))
      cursor <- parse(streams).map(_.hcursor)
    } yield cursor
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
  def getStreamsFromString(page: String, streamsWanted: StreamsWanted.Value): F[List[Format]] = for {
    cfg <- Sync[F].fromEither(getPlayerConfig(page))
    playerUlr <- Sync[F].fromEither(getPlayerUrl(cfg))
    streams <- Sync[F].fromEither(extractStreamsUrl(cfg))
    decipher <- getDecipher(playerUlr) match {
                  case Right(decipher) =>
                    Sync[F].pure(decipher)
                  case Left(NotKnownYet(pl)) =>
                    readStringFromUrl(pl.v).flatMap(body => Sync[F].fromEither(registerPlayer(pl, body)))
                  case Left(e) =>
                    Sync[F].raiseError[String => String](e)
                }

    video <- Sync[F].fromEither( if (streamsWanted == StreamsWanted.video || streamsWanted == StreamsWanted.all)
                              buildDownloadLinks(streams.video, decipher)
                            else
                              List[Format]().asRight)
    adaptive <- Sync[F].fromEither( if (streamsWanted == StreamsWanted.audio || streamsWanted == StreamsWanted.all)
                              buildDownloadLinks(streams.adaptive, decipher)
                            else
                              List[Format]().asRight)
    } yield {
      video ++ adaptive
    }

  /**
    * Scala oriented method that returns possible video streams.
    *
    * @param url video url of form `https://www.youtube.com/watch?v=ecekSCX3B4Q`
    * @return option contains map of type to video url
    */
  def getStreams(url: String, streamsWanted: StreamsWanted.Value = StreamsWanted.video): F[List[Format]] =
    readStringFromUrl(url).flatMap(getStreamsFromString(_, streamsWanted))
}

object YouTubeQuery {
  object StreamsWanted extends Enumeration {
    val all, video, audio = Value
  }

  private val logger = Logger(getClass.getName)

  protected[yt] val ReqConfig: RequestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000)
    .setConnectTimeout(5000).setRedirectsEnabled(true).build()
  lazy val ModernBrowser = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Safari/605.1.15 Chrome/80.0.3987.149 Firefox/74.0"

//  val PlayerConfigRegex: UnanchoredRegex = """(?i)ytplayer\.config\s*=\s*(\{.*\});\s*ytplayer\.load""".r.unanchored
  val PlayerConfigRegex: UnanchoredRegex = """(?i)ytplayer\.config\s*=\s*(\{.*\});ytplayer\.""".r.unanchored
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