package ru.shubert.yt

/**
  * Helper object, describes exact tag meaning. Might be useful in rendering download choices or describing stream
  * to external system. Latter case could require json representation.
  *
  * Tags are specified in YouTube links and leans the exact codec and type of the stream. For example, there could be
  * video only and audio only streams, low or high resolution streams and different codec like ogg, mp4, flw and so on.
  *
  * @see http://www.genyoutube.net/formats-resolution-youtube-videos.html
  */
object YouTubeConstants {

  case class StreamInfo(itag: Int, short: String, tech: String)

  val StreamTypes: Map[Int, StreamInfo] = Map[Int, StreamInfo](
    5 → StreamInfo(5, "240p flv", "type=video/x-flv"),
    17 → StreamInfo(17, "114p mpeg 3gpp", "type=video/3gpp;codecs=mp4v.20.3,+mp4a.40.2"),
    18 → StreamInfo(18, "360p mpeg", "type=video/mp4;codecs=avc1.42001E,+mp4a.40.2"),
    22 → StreamInfo(22, "720p mpeg", "type=video/mp4;codecs=avc1.64001F,+mp4a.40.2"),
    34 → StreamInfo(34, "360p flv", "type=video/x-flv"),
    35 → StreamInfo(35, "480p flv", "type=video/x-flv"),
    36 → StreamInfo(36, "240p mpeg 3gpp", "type=video/3gpp;codecs=mp4v.20.3,+mp4a.40.2"),
    37 → StreamInfo(37, "1080p mpeg", "type=video/mp4;codecs=avc1.64001F,+mp4a.40.2"),
    43 → StreamInfo(43, "360p webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    44 → StreamInfo(44, "480p webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    45 → StreamInfo(45, "720p webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    46 → StreamInfo(46, "1080p webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    82 → StreamInfo(82, "360p 3d mpeg", "type=video/mp4;codecs=avc1.42001E,+mp4a.40.2"),
    84 → StreamInfo(84, "1080p 3d mpeg", "type=video/mp4;codecs=avc1.64001F,+mp4a.40.2"),
    100 → StreamInfo(100, "1080p 3d webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    102 → StreamInfo(102, "360p 3d webm", "type=video/webm;codecs=vp8.0,+vorbis"),
    133 → StreamInfo(133, "240p mpeg", "type=video/mp4;codecs=avc1.4d4015;size=426x240"),
    134 → StreamInfo(134, "360p mpeg", "type=video/mp4;codecs=avc1.4d401e;size=640x360"),
    135 → StreamInfo(135, "480p mpeg", "type=video/mp4;codecs=avc1.4d401f;size=854x480"),
    136 → StreamInfo(136, "720p mpeg", "type=video/mp4;codecs=avc1.4d401f;size=1280x720"),
    137 → StreamInfo(137, "1080p mpeg", "type=video/mp4;codecs=avc1.640028;size=1920x1080"),
    138 → StreamInfo(138, "2304p mpeg", "type=video/mp4;codecs=avc1.640033;size=4096x2304"),
    140 → StreamInfo(140, "mpeg audio only", "type=audio/mp4;codecs=mp4a.40.2;bitrate=127949"),
    160 → StreamInfo(160, "144p mpeg", "type=video/mp4;codecs=avc1.42c00c;size=256x144"),
    171 → StreamInfo(171, "ogg vorbis audio only", "audio/webm;codecs=vorbis;bitrate=127949"),
    172 → StreamInfo(171, "ogg vorbis audio only", "audio/webm;codecs=vorbis;bitrate=127949"),
    242 → StreamInfo(242, "240p webm", "type=video/webm;codecs=vp9"),
    243 → StreamInfo(243, "360p webm", "type=video/webm;codecs=vp9"),
    244 → StreamInfo(244, "480p webm", "type=video/webm;codecs=vp9;size=854x480"),
    247 → StreamInfo(247, "720p mpeg", "type=video/webm;codecs=vp9;size=1280x720"),
    248 → StreamInfo(248, "1080p mpeg", "type=video/webm;codecs=vp9;size=1920x1080"),
    249 → StreamInfo(249, "webm audio only", "type=audio/webm;codecs=opus;bitrate=53372"),
    250 → StreamInfo(250, "webm audio only", "type=audio/webm; codecs=opus;bitrate=70588"),
    251 → StreamInfo(251, "webm audio only", "type=audio/webm; codecs=opus;bitrate=135398"),
    264 → StreamInfo(264, "1440p mpeg", "type=video/mp4;codecs=avc1.640032;size=2560x1440"),
    266 → StreamInfo(266, "2160p mpeg", "type=video/mp4;codecs=avc1.640033;size=3840x2160"),
    271 → StreamInfo(271, "1440p webm", "type=video/webm;codecs=vp9;size=2560x1440"),
    272 → StreamInfo(272, "2160p webm", "type=video/webm;codecs=vp9;size=3840x2160"),
    278 → StreamInfo(278, "144p webm", "type=video/webm;codecs=vp9;size=256x144"),
    298 → StreamInfo(278, "720p mpeg", "type=video/mp4;codecs=avc1.4d4020;size=1280x720"),
    299 → StreamInfo(278, "1080p mpeg", "type=video/mp4;codecs=avc1.4d4020;size=1920x1080"),
    302 → StreamInfo(302, "720p webm", "type=video/webm;codecs=vp9;size=1280x720"),
    303 → StreamInfo(303, "1080p webm", "type=video/webm;codecs=vp9;size=1920x1080"),
    308 → StreamInfo(308, "1440p webm", "type=video/webm;codecs=vp9;size=2560x1440"),
    313 → StreamInfo(313, "2160p webm", "type=video/webm;codecs=vp9;size=3840x2160"),
    315 → StreamInfo(315, "2160p webm", "type=video/webm;codecs=vp9;size=3840x2160"),
    330 → StreamInfo(330, "144p webm", "type=video/webm;codecs=vp9.2;size=256x144"),
    331 → StreamInfo(331, "240p webm", "type=video/webm;codecs=vp9.2;size=426x240"),
    332 → StreamInfo(332, "360p webm", "type=video/webm;codecs=vp9.2;size=640x360"),
    333 → StreamInfo(333, "480p webm", "type=video/webm;codecs=vp9.2;size=854x480"),
    334 → StreamInfo(334, "720p webm", "type=video/webm;codecs=vp9.2;size=1280x720"),
    335 → StreamInfo(335, "1080p webm", "type=video/webm;codecs=vp9.2;size=1920x1080"),
    336 → StreamInfo(336, "1440p webm", "type=video/webm;codecs=vp9.2;size=2560x1440"),
    337 → StreamInfo(337, "2160p webm", "type=video/webm;codecs=vp9.2;size=3840x2160")
  ).withDefaultValue(StreamInfo(0, "unknown", "unknown"))
}