package ru.shubert.yt

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

import cats.implicits._
import com.typesafe.scalalogging.Logger
import io.circe.HCursor
import io.circe.parser.parse
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import ru.shubert.yt.YGException.YGParseException
import ru.shubert.yt.YouTubeQuery.Format

import scala.collection.concurrent.TrieMap
import scala.collection.parallel.CollectionConverters._
import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
  *  Intended to be pure
 */
trait StreamParser {
  import StreamParser._

  // Extract video+audio streams and converts from escaped to plain
  protected def extractStreamsUrl(cfg: HCursor): Either[Exception, StreamsHolder] = {
    import io.circe.generic.auto._

    (for {
      root <- cfg.downField("args").get[String]("player_response")
      parsed <- parse(root)
      data = parsed.hcursor.downField("streamingData")
      video <- data.downField("formats").as[List[Format]]
      adaptive <- data.downField("adaptiveFormats").as[List[Format]]
    } yield {
      logger.info("Video streams: {}, adaptive {}", video.size, adaptive.size)
      StreamsHolder(video, adaptive)
    }).leftMap{ e =>
      logger.error("Failed to read streams.", e)
      e
    }
  }


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
  protected def buildDownloadLinks(urls: List[Format], decipher: String => String): Either[Throwable, List[Format]] = {
    def getSingleStream(desc: String, isQuery: Boolean) = {
      // Why so complicated? Youtube servers rejects requests with : 1.duplicate tags (!!!), 2.with + replaced with ' ', 3.on some urldecodings.
      // So here we doing our best not to interfere with params.
      if (isQuery) {
        val params1 = URLEncodedUtils.parse(desc, StandardCharsets.UTF_8).asScala
        val splitUrl = params1.partition(_.getName == "url")
        val urlExploded = splitUrl._1.head.getValue.split("\\?")
        val decodedLine = URLDecoder.decode(urlExploded(1), StandardCharsets.UTF_8.name())
        val params = splitUrl._2 ++ URLEncodedUtils.parse(decodedLine, StandardCharsets.UTF_8).asScala
        SingleStream(urlExploded(0), params)
      } else {
        val exploded = desc.split("\\?", 2)
        val url = exploded.head
        val params = URLEncodedUtils.parse(exploded(1), StandardCharsets.UTF_8).asScala
        SingleStream(url, params)
      }
    }
    val seen = new TrieMap[String, Either[Throwable, String]]()

    val k = urls.par.map { desc =>
        val singleStream: SingleStream = getSingleStream(desc.signatureCipher.orElse(desc.url).get, desc.signatureCipher.isDefined)
        val taggedStream = singleStream.params.foldLeft(TagStream()) { case (acc, pair) =>
          pair.getName match {
            case "sp" => acc.copy(signatureName = pair.getValue.some)
            case "signature" | "sig" => acc.copy(signature = pair.getValue.asRight, signatureName = acc.signatureName.orElse(pair.getName.some))
            case "s" | "sign" =>
                val sign = seen.getOrElseUpdate(pair.getValue, Either.catchNonFatal(decipher(pair.getValue)))
                acc.copy(signature = sign)
            case _ => // since 2016 youtube denies urls with empty params.
              if (pair.getValue != null && !pair.getValue.trim.isEmpty) {
                acc.params.add(pair)
              }
              acc
          }
        }

        for {
          sig <- taggedStream.signature
        } yield {
          taggedStream.params.add(new BasicNameValuePair(taggedStream.signatureName.get, sig))
          taggedStream.params.add(new BasicNameValuePair("itag", desc.itag.toString))
          val link = singleStream.url + "?" + URLEncodedUtils.format(taggedStream.params.toList.asJava, StandardCharsets.UTF_8)
          desc.copy(url = link.some)
        }
    }
    k.toList.sequence
  }
}

object StreamParser {
  private val logger = Logger(getClass.getName)

  //  noinspection ConvertExpressionToSAM
  implicit val ordering: Ordering[NameValuePair] = new Ordering[NameValuePair] {
    override def compare(x: NameValuePair, y: NameValuePair): Int = x.getName.compare(y.getName)
  }

  final case class StreamsHolder(video: List[Format], adaptive: List[Format])
  final case class SingleStream(url: String, params: mutable.Buffer[NameValuePair])

  case class TagStream(signatureName: Option[String] = None,
                       signature: Either[Throwable, String] = defaultSignature.asLeft,
                       params: mutable.TreeSet[NameValuePair] = mutable.TreeSet[NameValuePair]())
  lazy val defaultSignature: YGException  = YGParseException("Signature not found")
}