#!/bin/ksh

TARGET=plugin.video.lazyman.nhl.tv.zip 
ZIP=/usr/bin/zip
rm $TARGET
$ZIP -r $TARGET plugin.video.lazyman.nhl.tv/
