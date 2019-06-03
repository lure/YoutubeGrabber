# YoutubeGrabber 
[![Build Status](https://travis-ci.org/lure/YoutubeGrabber.svg?branch=master)](https://travis-ci.org/lure/YoutubeGrabber)
[![Maven Central](https://img.shields.io/maven-central/v/ru.shubert/youtubegrabber_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/ru.shubert/youtubegrabber_2.12)


Module that extracts downloadable video and audio streams from youtube. Basically you want exactly one you `YouTubeQuery` 
instance across all your application, as decipher engine may be somewhat costly to create and keep around in multiple instances.
Sometimes streams are protected by different signature type and in some cases for very new player.js or obfuscation methods 
this implementation may be unable to decrypt signature. in that case appropriate message will be logged and stream will 
be omitted from output map.
 


**Usage:** 

To use the grabber, you'll need to provide any biased container, or anything for what MonadError typeclass implementation exists / may be implemented.

```
  import cats.implicits._
  val grabber = new YouTubeQuery[Try]
  // or
  val grabber = {
    import monix.eval.Task
    new YouTubeQuery[Task]
  }
  // and now 
  for { 
    streams <- grabber.getStreams(topVideoUrl) // handle the result    
    _ <- doSomething(streams)
  } yield ... 
``` 

`YouTubeConstants.scala` holds a dictionary of known stream types, use it to render basic information to user if needed.   

**Adding things to projects:**

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
