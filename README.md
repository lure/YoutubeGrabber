# YoutubeGrabber [![Build Status](https://travis-ci.org/lure/YoutubeGrabber.svg?branch=master)](https://travis-ci.org/lure/YoutubeGrabber)

Module that extracts downloadable video and audio streams from youtube. 
Usage: 

```
  Scala: new YouTubeQuery{
      override protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  }.getStreams("https://www.youtube.com/watch?v=tO01J-M3g0U")
  
  
  Java: YouTubeQuery.getDefaultInstance().getJavaStreams("https://www.youtube.com/watch?v=tO01J-M3g0U")
``` 

sbt:
```
libraryDependencies += "ru.shubert" %% "youtubegrabber" % "1.5"
```

Maven 
```
<properties>
  ...
  <scala.major>2.12</scala.major>
</properties>
<dependency>
  <groupId>ru.shubert</groupId>
  <artifactId>youtubegrabber_${scala.major}</artifactId>
  <version>1.5</version>
</dependency>
```

https://issues.sonatype.org/browse/OSSRH-34359