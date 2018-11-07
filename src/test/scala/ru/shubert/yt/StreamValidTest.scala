package ru.shubert.yt

import cats.implicits._
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.impl.client.HttpClients
import org.scalatest.TryValues._
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.util.matching.UnanchoredRegex

/**
  * Picking up youtube official hot news stream, trying to determine available streams from it.
  * failing to do so means something changed and either test either extractor should be updated.
  * Author: Alexandr Shubert
  */
class StreamValidTest extends FlatSpecLike with Matchers {
  // YouTube's TopStories news channel
  val NewsChannel = "https://www.youtube.com/playlist?list=PL3ZQ5CpNulQnKJW0h8LQ3fJzgM34nLCxu"
  val TopVideoRE: UnanchoredRegex = """href="(/watch\?v=[^"]*)""".r.unanchored
  val HttpsYouTubeCom = "https://www.youtube.com"

  "download method" should "return downloaded page" in {
    val ytq: YouTubeQuery[Try] = new YouTubeQuery[Try]
    ytq.readStringFromUrl(NewsChannel).success.value should startWith regex "\\s*<!DOCTYPE html><html"
  }

  "All found stream urls" should "be accessible" in {
    val ytq: YouTubeQuery[Try] = new YouTubeQuery[Try]
    val newsLine = ytq.readStringFromUrl(NewsChannel)

    TopVideoRE.findFirstMatchIn(newsLine.success.get) match {
      case Some(u) =>
        val client = HttpClients.createDefault()
        val topVideoUrl = HttpsYouTubeCom + u.group(1)

        val mapResult = ytq.getStreams(topVideoUrl).success.get
        val errors = mapResult.foldLeft(List.empty[String]) {
          case (acc, (_, link)) =>
            val headMethod = new HttpHead(link)
            headMethod.setConfig(ytq.ReqConfig)
            try {
              val status = client.execute(headMethod).getStatusLine.getStatusCode
              if (status != 200) {
                s"$link returned status $status" :: acc
              } else {
                acc
              }
            } catch {
              case e: Exception => s"$topVideoUrl produced exception ${e.getMessage}" :: acc
            }
        }
        HttpClientUtils.closeQuietly(client)
        errors shouldBe 'empty

      case _ => fail("Unable to extract top video url. Fix the test pls!")
    }
  }

  "future subsctitution" should "should work" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val ytq: YouTubeQuery[Future] = new YouTubeQuery[Future]
    val future = ytq.readStringFromUrl(NewsChannel)
    val result = Await.result(future, Duration.Inf)
    result should startWith regex "\\s*<!DOCTYPE html><html"
  }

}
