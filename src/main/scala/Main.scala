import ru.shubert.yt.YouTubeQuery
import cats.implicits._
import scala.util.Try

object Main extends App {
  val result = new YouTubeQuery[Try]().getStreams(args.head)
//  val filtered = result.get.filter{case (k,v) => v.mimeType.startsWith("video")}

  result.get.values.toList.sortBy(_.mimeType).foreach{ v =>
    println(s"${v.mimeType} ${v.quality.getOrElse("")} ${v.qualityLabel.getOrElse("")}\n${v.url.get}\n")
  }
}
