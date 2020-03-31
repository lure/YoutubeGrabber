package ru.shubert.yt

object CurrentFormats extends App {
//  val str = """{
//              |  "streamingData": {
//              |    "expiresInSeconds": "21540",
//              |    "formats": [
//              |      {
//              |        "itag": 18,
//              |        "mimeType": "video/mp4;codecs=\"avc1.42001E,mp4a.40.2\"",
//              |        "bitrate": 627480,
//              |        "width": 640,
//              |        "height": 360,
//              |        "lastModified": "1575718399974522",
//              |        "contentLength": "25798169",
//              |        "quality": "medium",
//              |        "qualityLabel": "360p",
//              |        "projectionType": "RECTANGULAR",
//              |        "averageBitrate": 627349,
//              |        "audioQuality": "AUDIO_QUALITY_LOW",
//              |        "approxDurationMs": "328980",
//              |        "audioSampleRate": "44100",
//              |        "audioChannels": 2,
//              |        "cipher": "s=yd%3Dw%3D%3Dw6S05Ylv-kn3ou8dQ-Z6aVkY80dbd0iqVrGoxCW6RwbCQICYAyX0qdSQ_bEevTpg%3DLtoI6uRijH1_aKglK39f_LROHgIQRwMGkhKDtDt\u0026sp=sig\u0026url=https%3A%2F%2Fr5---sn-n8v7kn7e.googlevideo.com%2Fvideoplayback%3Fexpire%3D1583380246%26ei%3DtiJgXterLYyq7QSurrDoBQ%26ip%3D93.80.146.111%26id%3Do-ANlvJ09dsAu8JfNdJo61JJakkVNxwEaVlSwnpSQi4P5F%26itag%3D18%26source%3Dyoutube%26requiressl%3Dyes%26mm%3D31%252C26%26mn%3Dsn-n8v7kn7e%252Csn-4g5ednse%26ms%3Dau%252Conr%26mv%3Dm%26mvi%3D4%26pl%3D23%26initcwndbps%3D2185000%26vprv%3D1%26mime%3Dvideo%252Fmp4%26gir%3Dyes%26clen%3D25798169%26ratebypass%3Dyes%26dur%3D328.980%26lmt%3D1575718399974522%26mt%3D1583355699%26fvip%3D5%26fexp%3D23842630%26beids%3D9466585%26c%3DWEB%26txp%3D5531432%26sparams%3Dexpire%252Cei%252Cip%252Cid%252Citag%252Csource%252Crequiressl%252Cvprv%252Cmime%252Cgir%252Cclen%252Cratebypass%252Cdur%252Clmt%26lsparams%3Dmm%252Cmn%252Cms%252Cmv%252Cmvi%252Cpl%252Cinitcwndbps%26lsig%3DABSNjpQwRQIgDU1AVFxdlBKIZN-TQp86ec4FJ0So_4JgrpuAw-IQx64CIQD7uhhMERjo0frvMVaw77OT6Fm6jnIpOgnwaC-UK5oASA%253D%253D"
//              |      },
//              |      {
//              |        "itag": 22,
//              |        "mimeType": "video/mp4; codecs=\"avc1.64001F, mp4a.40.2\"",
//              |        "bitrate": 1709422,
//              |        "width": 1280,
//              |        "height": 720,
//              |        "lastModified": "1575718638808844",
//              |        "quality": "hd720",
//              |        "qualityLabel": "720p",
//              |        "projectionType": "RECTANGULAR",
//              |        "audioQuality": "AUDIO_QUALITY_MEDIUM",
//              |        "approxDurationMs": "328980",
//              |        "audioSampleRate": "44100",
//              |        "audioChannels": 2,
//              |        "cipher": "s=p78B%3D8Bj4aryfIsXrEnuowleSy2AJKgYHM7pS75M3VeGrzepAEiAHiAJuecl-lQ0ff72yi%3Du0pu6auDiI_pcw8dUpuafk1IAhIgRwMGkhKDFDF\u0026sp=sig\u0026url=https%3A%2F%2Fr5---sn-n8v7kn7e.googlevideo.com%2Fvideoplayback%3Fexpire%3D1583380246%26ei%3DtiJgXterLYyq7QSurrDoBQ%26ip%3D93.80.146.111%26id%3Do-ANlvJ09dsAu8JfNdJo61JJakkVNxwEaVlSwnpSQi4P5F%26itag%3D22%26source%3Dyoutube%26requiressl%3Dyes%26mm%3D31%252C26%26mn%3Dsn-n8v7kn7e%252Csn-4g5ednse%26ms%3Dau%252Conr%26mv%3Dm%26mvi%3D4%26pl%3D23%26initcwndbps%3D2185000%26vprv%3D1%26mime%3Dvideo%252Fmp4%26ratebypass%3Dyes%26dur%3D328.980%26lmt%3D1575718638808844%26mt%3D1583355699%26fvip%3D5%26fexp%3D23842630%26beids%3D9466585%26c%3DWEB%26txp%3D5532432%26sparams%3Dexpire%252Cei%252Cip%252Cid%252Citag%252Csource%252Crequiressl%252Cvprv%252Cmime%252Cratebypass%252Cdur%252Clmt%26lsparams%3Dmm%252Cmn%252Cms%252Cmv%252Cmvi%252Cpl%252Cinitcwndbps%26lsig%3DABSNjpQwRQIgDU1AVFxdlBKIZN-TQp86ec4FJ0So_4JgrpuAw-IQx64CIQD7uhhMERjo0frvMVaw77OT6Fm6jnIpOgnwaC-UK5oASA%253D%253D"
//              |      }
//              |    ]
//              |  }
//              |}""".stripMargin
//
//  val str_url = """[{
//                  |		"itag": 18,
//                  |		"url": "https://r2---sn-8xgp1vo-xfgs.googlevideo.com/videoplayback?expire=1585212184&ei=txZ8Xu_NOquQ8gSQjoPQCg&ip=173.48.214.209&id=o-APIR2TU87FDl0WHBzjbAjU6VEGYM-Pg8Dk428B85R8K5&itag=18&source=youtube&requiressl=yes&mh=Vs&mm=31%2C29&mn=sn-8xgp1vo-xfgs%2Csn-ab5szn7r&ms=au%2Crdu&mv=m&mvi=1&pl=16&initcwndbps=1816250&vprv=1&mime=video%2Fmp4&gir=yes&clen=5160448&ratebypass=yes&dur=61.231&lmt=1447672977577067&mt=1585190488&fvip=5&fexp=23882514&c=WEB&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cgir%2Cclen%2Cratebypass%2Cdur%2Clmt&sig=ADKhkGMwRgIhALblo88shjgM1oFq2TwG2XijGjD7rMjrsQo7ug_e3PsqAiEAoQyig5Yn9JbT7qd1OA57ioTvi9pXeDFfz2jhNTdDUrg%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=ABSNjpQwRAIgLUwCpSk_PgdhmVZc6BNML0ppnw-uXv-v49af0SGGA5ACIBoEr_Hh-489V-_OYKR80xExyTD9KiWNtTlOL39Nv5_m",
//                  |		"mimeType": "video/mp4; codecs=\"avc1.42001E, mp4a.40.2\"",
//                  |		"bitrate": 674568,
//                  |		"width": 640,
//                  |		"height": 360,
//                  |		"lastModified": "1447672977577067",
//                  |		"contentLength": "5160448",
//                  |		"quality": "medium",
//                  |		"qualityLabel": "360p",
//                  |		"projectionType": "RECTANGULAR",
//                  |		"averageBitrate": 674226,
//                  |		"audioQuality": "AUDIO_QUALITY_LOW",
//                  |		"approxDurationMs": "61231",
//                  |		"audioSampleRate": "44100",
//                  |		"audioChannels": 2
//                  |	},
//                  |	{
//                  |		"itag": 22,
//                  |		"url": "https://r2---sn-8xgp1vo-xfgs.googlevideo.com/videoplayback?expire=1585212184&ei=txZ8Xu_NOquQ8gSQjoPQCg&ip=173.48.214.209&id=o-APIR2TU87FDl0WHBzjbAjU6VEGYM-Pg8Dk428B85R8K5&itag=22&source=youtube&requiressl=yes&mh=Vs&mm=31%2C29&mn=sn-8xgp1vo-xfgs%2Csn-ab5szn7r&ms=au%2Crdu&mv=m&mvi=1&pl=16&initcwndbps=1816250&vprv=1&mime=video%2Fmp4&ratebypass=yes&dur=61.231&lmt=1508491637052788&mt=1585190488&fvip=5&fexp=23882514&c=WEB&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cratebypass%2Cdur%2Clmt&sig=ADKhkGMwRQIhAKxhr-y2Kls1PkgYac8VwF8NRw8hPcRJeifRQyeVzbGSAiB5shz0XawoGZyLg3jpGw5dJ3y7IFtMZBfLI20kEpCC1Q%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=ABSNjpQwRAIgLUwCpSk_PgdhmVZc6BNML0ppnw-uXv-v49af0SGGA5ACIBoEr_Hh-489V-_OYKR80xExyTD9KiWNtTlOL39Nv5_m",
//                  |		"mimeType": "video/mp4; codecs=\"avc1.64001F, mp4a.40.2\"",
//                  |		"bitrate": 1575398,
//                  |		"width": 1280,
//                  |		"height": 720,
//                  |		"lastModified": "1508491637052788",
//                  |		"quality": "hd720",
//                  |		"qualityLabel": "720p",
//                  |		"projectionType": "RECTANGULAR",
//                  |		"audioQuality": "AUDIO_QUALITY_MEDIUM",
//                  |		"approxDurationMs": "61231",
//                  |		"audioSampleRate": "44100",
//                  |		"audioChannels": 2
//                  |	}
//                  |]""".stripMargin
//  def getSingleStream(desc: String) = {
//    import scala.collection.JavaConverters._
//    val params1 = URLEncodedUtils.parse(desc, StandardCharsets.UTF_8).asScala
//    val splitUrl = params1.partition(_.getName == "url")
//    val urlExploded = splitUrl._1.head.getValue.split("\\?")
//    val decodedLine = URLDecoder.decode(urlExploded(1), StandardCharsets.UTF_8.name())
//    val params = splitUrl._2 ++ URLEncodedUtils.parse(decodedLine, StandardCharsets.UTF_8).asScala
//    println("")
//    println(s"url = ${urlExploded.toList}")
//    println(s"params = $params")
//  }
//
//  val b = parse(str).getOrElse(Json.Null).hcursor.downField("streamingData")
//  val c = b.downField("formats").as[List[Format]]
////  println(c)
//  c.foreach(_.foreach(x => getSingleStream(x.url)))
}
