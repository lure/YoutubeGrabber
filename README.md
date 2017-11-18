# YoutubeGrabber [![Build Status](https://travis-ci.org/lure/YoutubeGrabber.svg?branch=master)](https://travis-ci.org/lure/YoutubeGrabber)

Module that extracts downloadable video and audio streams from youtube. 
Fast usage: 

```
  Scala: YouTubeQuery.getStreams("https://www.youtube.com/watch?v=tO01J-M3g0U")
  
  Java: YouTubeQuery.getJavaStreams("https://www.youtube.com/watch?v=tO01J-M3g0U")
``` 


**Long story:**
Every youtube video page contains a js block which initialise html5 player. Of course, there is aa flash player, too, but JS one is more easily to understand. 

Streams located in two params: `url_encoded_fmt_stream_map` for video+audio combined and `adaptive_fmts` for video or audio separate streams. Each link is made up from two let's call it parts:

1. everything what is going before `url=https:` and 
2. everything from this point till the `,` (comma) which separates streams. 

What you should know about initialising block is: some params may appear twice and you have to keep only one copy in your request. You can't predict which params may be dropped out so try to include everything you've found in a "raw" link. Special part of all of this is a signature that is required by backends. Beware, that signature may appear in a first or second part of link.

There is 3 types of signatures in a current time: 

 - `signature` - this one is plain and need no decipher, requires no handling.
 - `sig` - ciphered
 - `s` - ciphered

Having this blocks, player should prepare each link and request backend for stream chosen. It's done by concatenating all existing params excluding duplicates and append prepared signature. 

While you can carefully find all possible player version this likely is not the best solution. It seems better to download player from your application and extract the exact decoding function from it. This approach will work with ANY youtube video page. 

Despite of your decision, you still need the function. Most of the time this function may be found with this RegEx

    set\("signature",\s*(?:([^(]*).*)\);

Use any formatting tool to read it source. You'll find that the main function uses one addition so you should extract it too. After that it's quite easy to implement the function in java or (which I prefer better) to extract decipher function and keep it around.

Steps in short: 
 1. extract `url_encoded_fmt_stream_map` and `adaptive_fmts`
 2. split by `,`
 3. find decipher function in player and reimplement it in language of your choice or extract and `eval`. 


for example, here is a function extracted from http://s.ytimg.com/yts/jsbin/html5player-new-en_US-vflhlPTtB/html5player-new.js

    var fs = {
        Q2: function (a, b) {
            a.splice(0, b)
        }, cK: function (a) {
            a.reverse()
        }, yy: function (a, b) {
            var c = a[0];
            a[0] = a[b % a.length];
            a[b] = c
        }
    };
    function gs(a) {
        a = a.split("");
        fs.yy(a, 40);
        fs.Q2(a, 3);
        fs.yy(a, 53);
        fs.yy(a, 11);
        fs.Q2(a, 3);
        fs.cK(a, 8);
        fs.Q2(a, 3);
        fs.yy(a, 16);
        fs.cK(a, 75);
        return a.join("")
    };


Tag: Download video from youtube java scala :)
