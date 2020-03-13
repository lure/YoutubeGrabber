package ru.shubert.yt

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import org.apache.http.client.utils.URLEncodedUtils
import ru.shubert.yt.YouTubeQuery.Format

object J extends App {
  val str = """{
              |  "streamingData": {
              |    "expiresInSeconds": "21540",
              |    "formats": [
              |      {
              |        "itag": 18,
              |        "mimeType": "video/mp4;codecs=\"avc1.42001E,mp4a.40.2\"",
              |        "bitrate": 627480,
              |        "width": 640,
              |        "height": 360,
              |        "lastModified": "1575718399974522",
              |        "contentLength": "25798169",
              |        "quality": "medium",
              |        "qualityLabel": "360p",
              |        "projectionType": "RECTANGULAR",
              |        "averageBitrate": 627349,
              |        "audioQuality": "AUDIO_QUALITY_LOW",
              |        "approxDurationMs": "328980",
              |        "audioSampleRate": "44100",
              |        "audioChannels": 2,
              |        "cipher": "s=yd%3Dw%3D%3Dw6S05Ylv-kn3ou8dQ-Z6aVkY80dbd0iqVrGoxCW6RwbCQICYAyX0qdSQ_bEevTpg%3DLtoI6uRijH1_aKglK39f_LROHgIQRwMGkhKDtDt\u0026sp=sig\u0026url=https%3A%2F%2Fr5---sn-n8v7kn7e.googlevideo.com%2Fvideoplayback%3Fexpire%3D1583380246%26ei%3DtiJgXterLYyq7QSurrDoBQ%26ip%3D93.80.146.111%26id%3Do-ANlvJ09dsAu8JfNdJo61JJakkVNxwEaVlSwnpSQi4P5F%26itag%3D18%26source%3Dyoutube%26requiressl%3Dyes%26mm%3D31%252C26%26mn%3Dsn-n8v7kn7e%252Csn-4g5ednse%26ms%3Dau%252Conr%26mv%3Dm%26mvi%3D4%26pl%3D23%26initcwndbps%3D2185000%26vprv%3D1%26mime%3Dvideo%252Fmp4%26gir%3Dyes%26clen%3D25798169%26ratebypass%3Dyes%26dur%3D328.980%26lmt%3D1575718399974522%26mt%3D1583355699%26fvip%3D5%26fexp%3D23842630%26beids%3D9466585%26c%3DWEB%26txp%3D5531432%26sparams%3Dexpire%252Cei%252Cip%252Cid%252Citag%252Csource%252Crequiressl%252Cvprv%252Cmime%252Cgir%252Cclen%252Cratebypass%252Cdur%252Clmt%26lsparams%3Dmm%252Cmn%252Cms%252Cmv%252Cmvi%252Cpl%252Cinitcwndbps%26lsig%3DABSNjpQwRQIgDU1AVFxdlBKIZN-TQp86ec4FJ0So_4JgrpuAw-IQx64CIQD7uhhMERjo0frvMVaw77OT6Fm6jnIpOgnwaC-UK5oASA%253D%253D"
              |      },
              |      {
              |        "itag": 22,
              |        "mimeType": "video/mp4; codecs=\"avc1.64001F, mp4a.40.2\"",
              |        "bitrate": 1709422,
              |        "width": 1280,
              |        "height": 720,
              |        "lastModified": "1575718638808844",
              |        "quality": "hd720",
              |        "qualityLabel": "720p",
              |        "projectionType": "RECTANGULAR",
              |        "audioQuality": "AUDIO_QUALITY_MEDIUM",
              |        "approxDurationMs": "328980",
              |        "audioSampleRate": "44100",
              |        "audioChannels": 2,
              |        "cipher": "s=p78B%3D8Bj4aryfIsXrEnuowleSy2AJKgYHM7pS75M3VeGrzepAEiAHiAJuecl-lQ0ff72yi%3Du0pu6auDiI_pcw8dUpuafk1IAhIgRwMGkhKDFDF\u0026sp=sig\u0026url=https%3A%2F%2Fr5---sn-n8v7kn7e.googlevideo.com%2Fvideoplayback%3Fexpire%3D1583380246%26ei%3DtiJgXterLYyq7QSurrDoBQ%26ip%3D93.80.146.111%26id%3Do-ANlvJ09dsAu8JfNdJo61JJakkVNxwEaVlSwnpSQi4P5F%26itag%3D22%26source%3Dyoutube%26requiressl%3Dyes%26mm%3D31%252C26%26mn%3Dsn-n8v7kn7e%252Csn-4g5ednse%26ms%3Dau%252Conr%26mv%3Dm%26mvi%3D4%26pl%3D23%26initcwndbps%3D2185000%26vprv%3D1%26mime%3Dvideo%252Fmp4%26ratebypass%3Dyes%26dur%3D328.980%26lmt%3D1575718638808844%26mt%3D1583355699%26fvip%3D5%26fexp%3D23842630%26beids%3D9466585%26c%3DWEB%26txp%3D5532432%26sparams%3Dexpire%252Cei%252Cip%252Cid%252Citag%252Csource%252Crequiressl%252Cvprv%252Cmime%252Cratebypass%252Cdur%252Clmt%26lsparams%3Dmm%252Cmn%252Cms%252Cmv%252Cmvi%252Cpl%252Cinitcwndbps%26lsig%3DABSNjpQwRQIgDU1AVFxdlBKIZN-TQp86ec4FJ0So_4JgrpuAw-IQx64CIQD7uhhMERjo0frvMVaw77OT6Fm6jnIpOgnwaC-UK5oASA%253D%253D"
              |      }
              |    ]
              |  }
              |}""".stripMargin

  def getSingleStream(desc: String) = {
    import scala.collection.JavaConverters._
    // Why so complicate? Youtube servers rejects requests with : 1.duplicate tags (!!!), 2.with + replaced with ' ', 3.on some urldecodings.
    // So here we doing our best not to interfere with params.
    val params1 = URLEncodedUtils.parse(desc, StandardCharsets.UTF_8).asScala
    val splitUrl = params1.partition(_.getName == "url")
    val urlExploded = splitUrl._1.head.getValue.split("\\?")
    val decodedLine = URLDecoder.decode(urlExploded(1), StandardCharsets.UTF_8.name())
    val params = splitUrl._2 ++ URLEncodedUtils.parse(decodedLine, StandardCharsets.UTF_8).asScala
    println("")
    println(s"url = ${urlExploded.toList}")
    println(s"params = $params")
  }

  val b = parse(str).getOrElse(Json.Null).hcursor.downField("streamingData")
  val c = b.downField("formats").as[List[Format]]
//  println(c)
  c.foreach(_.foreach(x => getSingleStream(x.cipher)))
}