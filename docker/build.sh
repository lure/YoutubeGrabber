#!/usr/bin/env bash
cd ..
sbt assembly
version=$(cat ./version.sbt | sed  -n 's/[^"]*"\([0-9][0-9]*.[0-9][0-9]*\)"/\1/p')
cp ./target/scala-2.13/youtubegrabber-assembly-$version.jar ./docker/grabber.jar
cd ./docker
docker build .  -t lure/grabber:v$version

