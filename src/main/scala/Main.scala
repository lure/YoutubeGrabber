import ru.shubert.yt.YouTubeQuery
import cats.implicits._

import scala.util.Try

object Main extends App {

//  import cats.instances.either._
//  type ErrorOr[T] = Either[Throwable, T]
//  val result = new YouTubeQuery[ErrorOr]().getStreams(args.head)

  val result = new YouTubeQuery[Try]().getStreams(args.head, videoOnly = true)
  result.get.values.toList.sortBy(_.itag).foreach{ v =>
    println(s"${v.itag} ${v.mimeType} ${v.quality.getOrElse("")} ${v.qualityLabel.getOrElse("")}\n${v.url.get}\n")
  }
}
