package ru.shubert.yt

import cats.implicits._
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.impl.client.HttpClients
import org.scalatest.TryValues._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import scala.util.Try

class StreamValidTest extends AnyFlatSpecLike with Matchers {
  it should "handle 4k feed" in {
    val grabber = new YouTubeQuery[Try]
// world-wide network issues .... 
    testExtraction(grabber, "https://www.youtube.com/watch?v=9Yam5B_iasY", 24)
//    testExtraction(grabber, "https://www.youtube.com/watch?v=H1589qbXUGo", 12)
//    testExtraction(grabber, "https://www.youtube.com/watch?v=u0Z7EPh8oLU", 12)
//    testExtraction(grabber, "https://www.youtube.com/watch?v=epwKK7yM9CM", 12)
  }

  private def testExtraction(ytq: YouTubeQuery[Try], url: String, count: Int) = {
    val client = HttpClients.createDefault()
    val mapResult = ytq.getStreams(url).success.get
    mapResult.size should be >= count
    val errors = mapResult.foldLeft(List.empty[String]) {
      case (acc, (_, f)) =>
        val headMethod = new HttpHead(f.url.get)
        headMethod.setConfig(YouTubeQuery.ReqConfig)
        try {
          val status = client.execute(headMethod).getStatusLine.getStatusCode
          if (status != 200) {
            s"${f.cipher} returned status $status" :: acc
          } else {
            acc
          }
        } catch {
          case e: Exception => s"$url produced exception ${e.getMessage}" :: acc
        }
    }
    HttpClientUtils.closeQuietly(client)

    println(s"Found ${mapResult.size} streams, errored: ${errors.size}")
    errors.size shouldBe 0
  }
}
