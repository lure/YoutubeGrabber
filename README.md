# YoutubeGrabber [![Build Status](https://travis-ci.org/lure/YoutubeGrabber.svg?branch=master)](https://travis-ci.org/lure/YoutubeGrabber)

Module that extracts downloadable video and audio streams from youtube. 
Usage: 

```
  Scala: YouTubeQuery.getStreams("https://www.youtube.com/watch?v=tO01J-M3g0U")
  
  Java: YouTubeQuery.getJavaStreams("https://www.youtube.com/watch?v=tO01J-M3g0U")
``` 

sbt:
```
libraryDependencies += "ru.shubert" %% "youtubegrabber" % "1.4"
```

Maven 
```
<dependency>
  <groupId>ru.shubert</groupId>
  <artifactId>youtubegrabber_2.12</artifactId>
  <version>1.4</version>
</dependency>
```

