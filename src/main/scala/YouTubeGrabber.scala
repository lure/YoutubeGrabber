import java.io._
import java.net.{HttpURLConnection, MalformedURLException, URL, URLDecoder}
import java.util.logging.{Level, LogManager, Logger}

import org.apache.commons.lang3.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.annotation.tailrec


/**
 * Note: with default user-agent http protocol is forced to http and returned link doesn't work for download.
 * There is two commons sections and one that appears from time to time.
 * {{{url_encoded_fmt_stream_map}}} Combined streams, handful of codecs, seems up to p720
 * {{{adaptive_fmts}}} - separate streams, video or audio. HD+ may be accessed only via such streams.
 * Speaking of combined streams the next two video types are of particular interest according to TD.
 * {{{
 * 22  :   mp4 [1280 x 720]
 * 18  :   mp4 [640 x 360]
 * }}}
 * values mentioned above are stored in 'itag' url parameter.
 * It appears that all other combined types aren't mp4. Maybe there is another indexes, I didn't found them yet.
 * It seems they are offered depending on your user-agent and iApple takes it all ^.^
 *
 * youtube link appears to contain fallback part in url that must be stripped.
 *
 * NOTE: sometimes the url is broken and it's signature part comes into first line like
 * fallback_host=tc.v22.cache7.googlevideo.com\u0026itag=22\u0026type=video%2Fmp4%3B+codecs%3D%22avc1.64001F%2C+mp4a.40.2%22\u0026quality=hd720\u0026
 * It has to be done next time
 */
object YouTubeGrabber extends App {
  LogManager.getLogManager.readConfiguration(getClass.getResourceAsStream("logging.properties"))
  val logger = Logger.getLogger(getClass.toString)

  val CodePage = "UTF-8"
  val UserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.0.0 Safari/537.11 Firefox/34.0"
  val VideoPttr = """\"url_encoded_fmt_stream_map\":\s*\"([^\"]*)\"""".r
  val SeparatePttr = """\"adaptive_fmts\":\s*\"([^\"]*)\"""".r
  val TitlePttr = """.*\"title\":\s*\"([^\"]*)\".*""".r
  val FallbackToRemovePttr = """([^&,\"]*)""".r
  val VideoTypePttr = """[&\?]itag=(\d+)""".r
  val ScriptSelector: String = "div#player-mole-container script ~ script"

  val MP4_P720 = "22"
  val MP4_P360 = "18"

  val UnknownVideoTitle = "UnknownVideo"
  val FileExtension: String = ".mp4"

  val MSG_NoVideoFound: String = "No suitable video streams found. Check your url, please"
  val MSG_NoArgsProvided = "Error: no url provided. Usage: #java -jar https://www.youtube.com/watch?v=JGku8J7Wb6Y"
  val MSG_CONNECT_OK = "Connection successfull"
  val MSG_VIDEOCOUNT = "Media streams found:"
  val MSG_DONE = "Saved new video: "
  val MSG_DOWNLOAD_NOTIFY: String = "Downloading mp4..."

  downloadVideo(args)

  /**
   * Saves 720p or 360p mp4 video from youtube in a folder where app resides.
   * Link must be provided in url-uncoded, simple form that may be acquired from browser address field.
   * @param args command line parameters
   * @return 0 if succeed, 1 if any errors have been met.
   */
  def downloadVideo(args: Array[String]): Int =
    if (args.nonEmpty) {
      val url = args(0)
      getRemoteHtmlDocument(url) foreach { doc =>
        logger.log(Level.INFO, MSG_CONNECT_OK)
        val html = doc.select(ScriptSelector).html()

        val section = VideoPttr findFirstIn html
        val urls = getMediaUrlsBySection(section)
        logger.log(Level.INFO, MSG_VIDEOCOUNT + urls.size)

        Option(urls.getOrElse(MP4_P720, urls.getOrElse(MP4_P360, null))) match {
          case Some(video) => saveStreamToFile(video, getVideoTitle(html))
          case None => logger.log(Level.INFO, MSG_NoVideoFound)
        }
      }
      0
    } else {
      logger.log(Level.INFO, MSG_NoArgsProvided)
      1
    }

  def getRemoteHtmlDocument(url: String): Option[Document] = {
    try
      Some(Jsoup.connect(url).timeout(5000)
        .header("Accept-Charset", CodePage)
        .header("User-Agent", UserAgent)
        .get())
    catch {
      case e: Exception =>
        logger.log(Level.SEVERE, s"failed to access video url $url with:\n$e")
        None
    }
  }

  def getVideoTitle(html: String) = TitlePttr findFirstIn html match {
    case Some(TitlePttr(title)) => title
    case _ => UnknownVideoTitle
  }


  def getMediaUrlsBySection(section: Option[String]): Map[String, String] = {
    val urlList = section map (_.split("url="))

    val unescapedList = urlList.map(_ map StringEscapeUtils.unescapeJava)

    unescapedList match {
      case None => Map.empty
      case Some(list) => {
        for {
          str <- list
          dec = FallbackToRemovePttr findFirstIn str
          if dec.isDefined && dec.get.startsWith("http")
          fullUrlDecoded = fullUrlDecode(str)
          videoType = VideoTypePttr findFirstIn fullUrlDecoded match {
            case Some(VideoTypePttr(tag)) => Some(tag)
            case _ => None
          }
          signature = SignatureDecryptor(fullUrlDecoded)
        } yield videoType.getOrElse("?") -> (URLDecoder.decode(dec.get, CodePage) + signature) //fullUrlDecode(dec.get)
      }.toMap
    }
  }

  def saveStreamToFile(url: String, title: String) = {
    logger.log(Level.INFO, MSG_DOWNLOAD_NOTIFY)
    var bous: BufferedOutputStream = null
    try {
      //val execPath = getClass.getProtectionDomain.getCodeSource.getLocation
      val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")
      connection.setConnectTimeout(5000)
      connection.setReadTimeout(5000)

      val stream = connection.getInputStream
      val fileName = prepareFileName(title)
      val file = new File(fileName + FileExtension)
      if (!file.exists()) file.createNewFile()

      bous = new BufferedOutputStream(new FileOutputStream(file))
      val buffer = new Array[Byte](1024)

      Iterator.continually(stream.read(buffer)).takeWhile(-1 !=).foreach(size => bous.write(buffer, 0, size))

      logger.log(Level.INFO, MSG_DONE + file.getName)
    } catch {
      case e: MalformedURLException => logger.log(Level.SEVERE, s"failed to open connection with:\n$e \nfor '$url'")
      case e: Exception => logger.log(Level.SEVERE, s"failed to write video with:\n$e")
    } finally {
      if (bous != null) bous.close()
    }
  }

  @tailrec
  def fullUrlDecode(str: String): String =
    if (str.indexOf('%') != -1) fullUrlDecode(URLDecoder.decode(str, CodePage)) else str

  def prepareFileName(title: String) = {
    val FileNameExclusionSet: Set[Char] = Set[Char]('\\', '/', '?', ':', '*', '"', '>', '<', '|')
    title.filterNot(FileNameExclusionSet.contains).take(250).trim
  }


}