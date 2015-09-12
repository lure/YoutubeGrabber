package ru.shubert.yt

/**
 *
 * Author: Alexandr Shubert
 */
object YouTubeConstants {
  case class StreamInfo(itag: Int, short: String, tech: String)

  val StreamTypes = Map[Int, StreamInfo](
    5 -> StreamInfo(5,      "240p flv", "type=video/x-flv"),
    13 -> StreamInfo(13,    "small mpeg 3gpp", "type=video/3gpp;codecs=mp4v"),
    17 -> StreamInfo(17,    "114p mpeg 3gpp", "type=video/3gpp;codecs=mp4v.20.3,+mp4a.40.2"),
    18 -> StreamInfo(18,    "360p mpeg", "type=video/mp4;codecs=avc1.42001E,+mp4a.40.2"),
    22 -> StreamInfo(22,    "720p mpeg", "type=video/mp4;codecs=avc1.64001F,+mp4a.40.2"),
    34 -> StreamInfo(34,    "360p flv", "type=video/x-flv"),
    35 -> StreamInfo(35,    "480p flv", "type=video/x-flv"),
    36 -> StreamInfo(36,    "240p mpeg 3gpp", "type=video/3gpp;codecs=mp4v.20.3,+mp4a.40.2"),
    37 -> StreamInfo(37,    "1080p mpeg", "type=video/mp4;codecs=avc1.64001F,+mp4a.40.2"),
    43 -> StreamInfo(43,    "360p webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    44 -> StreamInfo(44,    "480p webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    45 -> StreamInfo(45,    "720p webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    46 -> StreamInfo(46,    "1080p webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    82 -> StreamInfo(82,    "360p 3d mpeg", "type=video/mp4;codecs=avc1.42001E,+mp4a.40.2"),
    84 -> StreamInfo(84,    "1080p 3d mpeg", "type=video/mp4;codecs=avc1.64001F,+mp4a.40.2"),
    100 -> StreamInfo(100,  "1080p 3d webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    102 -> StreamInfo(102,  "360p 3d webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    133 -> StreamInfo(133,  "240p mpeg", "type=video/mp4;codecs=avc1.4d4015;size=426x240"),
    134 -> StreamInfo(134,  "360p mpeg", "type=video/mp4;codecs=avc1.4d401e;size=640x360"),
    135 -> StreamInfo(135,  "480p mpeg", "type=video/mp4;codecs=avc1.4d401f;size=854x480"),
    136 -> StreamInfo(136,  "720p mpeg", "type=video/mp4;codecs=avc1.4d401f;size=1280x720"),
    137 -> StreamInfo(137,  "1080p mpeg", "type=video/mp4;codecs=avc1.640028;size=1920x1080"),
    138 -> StreamInfo(138,  "2304p mpeg", "type=video/mp4;codecs=avc1.640033;size=4096x2304"),
    140 -> StreamInfo(140,  "mpeg audio only", "type=audio/mp4;codecs=mp4a.40.2 bitrate=127949"),
    160 -> StreamInfo(160,  "144p mpeg", "type=video/mp4;codecs=avc1.42c00c;size=256x144"),
    171 -> StreamInfo(171,  "ogg vorbis audio only", "audio/webm;codecs=vorbis bitrate=127949"),
    242 -> StreamInfo(242,  "240p webm", "type=video/webm;codecs=vp9"),
    243 -> StreamInfo(243,  "360p webm", "type=video/webm;codecs=vp9"),
    244 -> StreamInfo(244,  "480p webm", "type=video/webm;codecs=vp9;size=854x480"),
    247 -> StreamInfo(247,  "1080p mpeg", "type=video/webm;codecs=vp9;size=1280x720"),
    248 -> StreamInfo(248,  "1080p mpeg", "type=video/webm;codecs=vp9;size=1920x1080"),
    249 -> StreamInfo(249,  "webm audio only", "type=audio/webm;codecs=opus@50k"),
    250 -> StreamInfo(250,  "webm audio only", "type=audio/webm;codecs=opus bitrate=71434"),
    251 -> StreamInfo(251,  "webm audio only", "type=audio/webm;codecs=opus@160k"),
    264 -> StreamInfo(264,  "1440p mpeg", "type=video/mp4;codecs=avc1.640032;size=2560x1440"),
    266 -> StreamInfo(266,  "2160p mpeg", "type=video/mp4;codecs=avc1.640033;size=3840x2160"),
    271 -> StreamInfo(271,  "1440p webm", "type=video/webm;codecs=vp9;size=2560x1440"),
    272 -> StreamInfo(272,  "2160p webm", "type=video/webm;codecs=vp9;size=3840x2160"),
    278 -> StreamInfo(278,  "144p webm", "type=video/webm;codecs=vp9;size=256x144")
  )
}
