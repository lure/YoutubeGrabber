package ru.shubert.yt

import org.apache.http.client.methods.HttpHead
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.impl.client.HttpClients
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpecLike, Matchers}

/**
 *
 * Author: Alexandr Shubert
 */

@RunWith(value = classOf[JUnitRunner])
class StreamValidTest extends FlatSpecLike with Matchers {
  // YouTube's TopStories news channel
  val NewsChannel = "https://www.youtube.com/playlist?list=PL3ZQ5CpNulQnKJW0h8LQ3fJzgM34nLCxu"
  val TopVideoRE = """href="(/watch\?v=[^"]*)""".r.unanchored
  val HttpsYouTubeCom = "https://www.youtube.com"


  "download method" should "return downloaded page" in {
    val result = YouTubeQuery.download(NewsChannel)
    result should be a 'success
    result.foreach(_ should startWith regex "\\s*<!DOCTYPE html><html")
  }


  "All found stream urls" should "be accessible" in {
    val news = YouTubeQuery.download(NewsChannel)
    news.foreach { body =>
      TopVideoRE.findFirstMatchIn(body) match {
        case Some(u) =>
          val client = HttpClients.createDefault()
          val topVideoUrl = HttpsYouTubeCom + u.group(1)
          YouTubeQuery.getStreams(topVideoUrl) match {
            case Some(streams) =>
              val errors = streams.foldLeft(List.empty[String]) {
                case (acc, (idx, link)) =>
                  val headMethod = new HttpHead(link)
                  headMethod.setConfig(YouTubeQuery.ReqConfig)
                  try {
                    val status = client.execute(headMethod).getStatusLine.getStatusCode
                    if (status != 200) {
                      s"$topVideoUrl returned status $status" :: acc
                    } else {
                      acc
                    }
                  } catch {
                    case e: Exception => s"$topVideoUrl produced exception ${e.getMessage}" :: acc
                  }
              }
              HttpClientUtils.closeQuietly(client)
              errors shouldBe 'empty

            case None => fail("No streams found for " + topVideoUrl)
          }
        case _ => fail("Unable to extract top video url. Fix the test pls!")
      }
    }
  }

}
