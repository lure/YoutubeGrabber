import ru.shubert.yt.YouTubeQuery
import cats.implicits._
import ru.shubert.yt.YouTubeQuery.StreamsWanted

import scala.util.{Failure, Success, Try}

object Main extends App {
  if (args.nonEmpty) {
    val mbStream = readStreamType
    if (mbStream.isEmpty) {
      printHelpAndExit(s"Unknown stream type ${args(1)}")
    }

    val result = new YouTubeQuery[Try]().getStreams(args.head, mbStream.getOrElse(StreamsWanted.video))

    result match {
      case Success(m) => m.sortBy(_.itag).foreach { v =>
        println(s"tag: ${v.itag} mime: ${v.mimeType} ${v.qualityLabel.getOrElse("")} ${v.quality.getOrElse("")} \n${v.url.get}\n")
      }
        System.exit(0)
      case Failure(e) => println(s" Failed to read streams: ${e.getMessage}")
        System.exit(500)
    }
  } else {
    printHelpAndExit("No arguments provided")
  }

  private def readStreamType: Option[StreamsWanted.Value] = {
    if (args.length > 1) {
      StreamsWanted.values.find(_.toString == args(1).toLowerCase())
    } else {
      Some(StreamsWanted.video)
    }
  }

  def printHelpAndExit(msg: String): Unit = {
    println(msg)
    println("parameters:\n\t url [video|audio|all]")
    println("Second parameter specifies media type. `video` is used by default")
    println("Example: https://www.youtube.com/watch?v=beXW5s3ZCB4 all")
    System.exit(400)
  }
}
