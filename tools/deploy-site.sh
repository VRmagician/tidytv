#!/bin/sh
# Deploys tidytv.winkle.dev (static files in site/). The app itself is on Google Play;
# /tidytv.apk redirects there (site/_redirects). Needs `npx wrangler login`.
set -e
cd "$(dirname "$0")/.."
rm -f site/tidytv.apk
npx --yes wrangler@4 deploy
