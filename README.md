# YoutubeGrabber [![Build Status](https://travis-ci.org/lure/YoutubeGrabber.svg?branch=master)](https://travis-ci.org/lure/YoutubeGrabber)

Module that extracts downloadable video and audio streams from youtube. Basically you want exactly one you `YouTubeQuery` 
instance across all your application, as decipher engine may be somewhat costly to create and keep around in multiple instances.
Sometimes streams are protected by different signature type and in some cases for very new player.js or obfuscation methods 
this implementation may be unable to decrypt signature. in that case appropriate message will be logged and stream will 
be omitted from output map.


`YouTubeConstants` holds a dictionary of known (to me) stream types, use it to render basic information to user if needed.   
 
Usage: 

```
  import cats.implicits._
  val grabber = new YouTubeQuery[Try]
  // or
  val grabber = {
    import scala.concurrent.ExecutionContext.Implicits.global
    new YouTubeQuery[Future]
  }

  grabberTry.getStreams(topVideoUrl) // handle the result    
 
``` 

Note that both calls return language-specific Future. This may change in next releases. 

sbt:
```
libraryDependencies += "ru.shubert" %% "youtubegrabber" % "2.0"
```

Maven 

```
<properties>
  ...
  <scala.major>%put your version here%</scala.major>
</properties>
<dependency>
  <groupId>ru.shubert</groupId>
  <artifactId>youtubegrabber_${scala.major}</artifactId>
  <version>2.0</version>
</dependency>
```