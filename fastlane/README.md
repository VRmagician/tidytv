# Publishing TidyTV to Google Play

TidyTV ships to Play with fastlane (`supply`), the same way as Essence and Flofocus. The listing lives in
`fastlane/metadata/android/en-IN/` (the Play listing's default language is English (India)). Store-changing lanes go
through winkle-guard, which asks Joe.

Setup, once per machine:

```bash
bundle install
export LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8 FASTLANE_SKIP_UPDATE_CHECK=1 FASTLANE_HIDE_CHANGELOG=1
```

The Play service account key is `~/.appfactory/keys/google-play-service-account.json` (see `Appfile`). Never copy or print it.

## Release steps

1. Tests green: `./gradlew :app:testDebugUnitTest` (plus any device checks for the release).
2. Bump `versionCode` (must be above every code on Play) and `versionName` in `app/build.gradle.kts`.
3. Write the Play notes for that code: `fastlane/metadata/android/en-IN/changelogs/<versionCode>.txt` (500 characters max).
4. Build and upload to the internal track:
   ```bash
   bundle exec fastlane android deploy_internal
   ```
   `SKIP_BUILD=1` reuses `app/build/outputs/bundle/release/app-release.aab`.
5. Install it from the internal track and check it on a phone and the TV.
6. Promote to production:
   ```bash
   CONFIRM=1 bundle exec fastlane android promote_to_production
   ```
   (`CONFIRM=1 bundle exec fastlane android deploy_production` builds and uploads straight to production instead.)
7. Read back the tracks:
   ```bash
   bundle exec fastlane android verify
   ```
8. Make the GitHub release with the signed APK, as before, for sideloaded copies (they check GitHub for updates).

## Listing and screenshots

```bash
bundle exec fastlane android metadata_download                        # pull the live listing (read-only)
bundle exec fastlane android metadata_upload validate_only:true       # check the text with Play, changes nothing
bundle exec fastlane android metadata_upload                          # push title, short and full description
bundle exec fastlane android metadata_upload version_code:8           # also push changelogs/8.txt
bundle exec fastlane android upload_screenshots                       # images/ and screenshots only
```

Limits: title 30, short description 80, full description 4000 characters.
