import cats.effect.{ExitCode, IO, IOApp}
import ru.shubert.yt.YouTubeQuery
import cats.implicits._
import ru.shubert.yt.YouTubeQuery.StreamsWanted

object Main extends IOApp {


  override def run(args: List[String]): IO[ExitCode] = {
      for {
        _ <- readStreamType(args) match {
          case Right((url, streamType)) =>
            new YouTubeQuery[IO]().getStreams(url, streamType) >>= ( streams =>
              streams.sortBy(_.itag).traverse( v =>
                IO.delay(println(s"tag: ${v.itag} mime: ${v.mimeType} ${v.qualityLabel.getOrElse("")} ${v.quality.getOrElse("")} \n${v.url.get}\n"))
                  *> IO.pure(ExitCode.Success)
            ))

          case Left((code, reason)) => IO.delay{println(reason); ExitCode(code)}
        }
      } yield ExitCode.Success
  }


  def readStreamType(args: List[String]): Either[(Int, String), (String, StreamsWanted.Value)] = {
    if (args.nonEmpty) {
      val mbStream = if (args.length > 1) {
        StreamsWanted.values.find(_.toString == args(1).toLowerCase()).map(args.head -> _)
      } else {
        Some(args.head -> StreamsWanted.video)
      }
      mbStream.toRight(400 -> errorMessage(s"Unknown argument ${args(1)}"))
    } else {
      Left(500 -> errorMessage(""))
    }
  }

  def errorMessage(msg: String): String = {
    s"""|${if (msg.nonEmpty) msg + "\n" else ""}parameters:\n\t url [video|audio|all]
        |Second parameter specifies media type. `video` is used by default
        |Example: https://www.youtube.com/watch?v=beXW5s3ZCB4 all
        |""".stripMargin
  }
}
