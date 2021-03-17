#!/usr/bin/env bash

cd "$(dirname "$0")"

apktool b .

$ANDROID_HOME/build-tools/31.0.0-rc1/zipalign -f 4 dist/FrameworkFlags.apk{,.new}
mv dist/FrameworkFlags.apk{.new,}
$ANDROID_HOME/build-tools/31.0.0-rc1/apksigner sign --ks-pass pass:abcdef --key-pass pass:abcdef --ks ../monet/out/test.jks dist/FrameworkFlags.apk
