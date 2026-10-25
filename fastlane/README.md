fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android metadata_download

```sh
[bundle exec] fastlane android metadata_download
```

Download the live Play listing into fastlane/metadata/android/ (read-only)

### android metadata_upload

```sh
[bundle exec] fastlane android metadata_upload
```

Upload listing text (and changelogs with version_code:N). validate_only:true checks without changing anything

### android upload_screenshots

```sh
[bundle exec] fastlane android upload_screenshots
```

Upload screenshots and images only (no AAB, no listing text)

### android deploy_internal

```sh
[bundle exec] fastlane android deploy_internal
```

Build the AAB and upload it to the internal testing track

### android deploy_production

```sh
[bundle exec] fastlane android deploy_production
```

Build the AAB and upload it to the production track for review (CONFIRM=1)

### android promote_to_production

```sh
[bundle exec] fastlane android promote_to_production
```

Promote the internal build to production (CONFIRM=1)

### android release_testing

```sh
[bundle exec] fastlane android release_testing
```

Copy the production build to closed testing (alpha) or open testing (beta). track:alpha|beta CONFIRM=1

### android verify

```sh
[bundle exec] fastlane android verify
```

Read back which version codes the internal and production tracks have

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
