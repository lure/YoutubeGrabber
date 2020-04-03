# YoutubeGrabber 
[![Build Status](https://travis-ci.org/lure/YoutubeGrabber.svg?branch=master)](https://travis-ci.org/lure/YoutubeGrabber)
[![Maven Central](https://img.shields.io/maven-central/v/ru.shubert/youtubegrabber_2.13.svg)](https://maven-badges.herokuapp.com/maven-central/ru.shubert/youtubegrabber_2.13)


Module that extracts downloadable video and audio streams from youtube. Basically you want exactly one you `YouTubeQuery` 
instance across all your application, as decipher engine may be somewhat costly to create and keep around in multiple instances.
Sometimes streams are protected by different signature type and in some cases for very new player.js or obfuscation methods 
this implementation may be unable to decrypt signature. in that case appropriate message will be logged and stream will 
be omitted from output map.
 


**Usage:** 
To use the grabber, you'll need to provide any biased container, or anything for what MonadError typeclass implementation exists / may be implemented.

```
  import cats.implicits._
  val result = new YouTubeQuery[Try]().getStreams(args.head)
``` 

`videoOnly = true` will return just two or three streams of video paired with audio. Otherwise all the available streams 
will be returned. Youtube does not provide 1080+audio anymore (may be it is temporary decision).  

`YouTubeConstants.scala` holds a dictionary of known stream types, use it to render basic information to user if needed.   

**Adding things to projects:**

SBT:

```
libraryDependencies += "ru.shubert" %% "youtubegrabber" % "2.8"
```

Gradle

```
implementation "ru.shubert:youtubegrabber_{scala_version}:2.8"
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

## Docker

Existing image 
```
$ docker pull lure/grabber:v2.8
$ docker run --rm lure/grabber:v2.8 https://www.youtube.com/watch?v=I3loMuHnYqw
```

Or rebuild from sources
```
$./docker/build.sh
$ docker run --rm lure/grabber:v2.8 https://www.youtube.com/watch?v=I3loMuHnYqw
```