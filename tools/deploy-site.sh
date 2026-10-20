#!/bin/sh
# Builds the signed release APK, puts it on the site as tidytv.apk and deploys tidytv.winkle.dev.
# Needs the TIDYTV_* signing properties in ~/.gradle/gradle.properties and `npx wrangler login`.
set -e
cd "$(dirname "$0")/.."
./gradlew -q :app:assembleRelease
cp app/build/outputs/apk/release/app-release.apk site/tidytv.apk
npx --yes wrangler@4 deploy
