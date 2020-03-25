package ru.shubert.yt

import cats.implicits._
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.impl.client.HttpClients
import org.scalatest.TryValues._
import org.scalatest.{FlatSpecLike, Matchers}
import scala.util.Try
import scala.util.matching.UnanchoredRegex

/**
  * Picking up youtube official hot news stream, trying to determine available streams from it.
  * failing to do so means something changed and either test either extractor should be updated.
  * Author: Alexandr Shubert
  */
class StreamValidTest extends FlatSpecLike with Matchers {
  // YouTube's TopStories news channel
  val NewsChannel = "https://www.youtube.com/"
  val TopVideoRE: UnanchoredRegex = """(?:(?:href=)|(?:url":))"(/watch\?v=[^"]*)""".r.unanchored
  val HttpsYouTubeCom = "https://www.youtube.com"

//  "download method" should "return downloaded page" in {
//    val ytq: YouTubeQuery[Try] = new YouTubeQuery[Try]
//    ytq.readStringFromUrl(NewsChannel).success.value should startWith regex "\\s*(?i)<!DOCTYPE html><html"
//  }

//  it should "handle 4k feed" in {
//    testExtraction(new YouTubeQuery[Try], "https://www.youtube.com/watch?v=9Yam5B_iasY", 24)
//  }

  private def testExtraction(ytq: YouTubeQuery[Try], url: String, count: Int) = {
    val client = HttpClients.createDefault()
    val mapResult = ytq.getStreams(url).success.get
    mapResult.size should be >= count
    val errors = mapResult.foldLeft(List.empty[String]) {
      case (acc, (_, f)) =>
        val headMethod = new HttpHead(f.cipher)
        headMethod.setConfig(ytq.ReqConfig)
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
    errors shouldBe 'empty
  }
}
