# tidytv roadmap

Things deliberately left out of v1. v1 is: connect with no code, back up, turn bloat off, optional
tv-detox home screen, restore everything. Everything below comes later.

## Launcher choices
- cleanyourtv's Apple TV style launcher, built and signed in our CI (Android 8+, `config.json` themes,
  accessibility-safe restart).
- **Projectivy**, opened on the TV's Play Store page. Never bundled.
- Measure each launcher's RAM with `dumpsys meminfo` before showing numbers. Known so far: tv-detox
  launcher is a 37 KB APK using about 30 MB RAM; the cleanyourtv launcher is about 300 KB.

## Mascot and art
- Full mascot set: waving, searching, look-at-TV, backup, broom, confetti, worried, sleeping.
- A "press Allow" TV illustration, developer-options illustrations, a launcher preview, a README
  banner, adaptive-icon polish.

## More checks and safety
- **Check my TV**: re-apply after firmware updates (tv-detox `check.sh`).
- **Reboot test** at the end: wait for adbd to come back, then re-verify the disabled set and Home.
- **Boot-loop rescue**: keep retrying the connection and enable everything as soon as ADB answers.

## Extra tweaks
- Animation scale 0.5, screensaver off, sleep timer, ad-blocking Private DNS. Each one is backed up
  first and restored from the backup value, never a hardcoded one (the Restorer already does this
  for any setting recorded in the action log).
- Microphone permission revoke recipe (from the TCL list notes).

## Lists
- Fetch newer lists from raw.githubusercontent.com at runtime, validated by the same parser.
- **Share my working list**: open a pre-filled upstream `tv-list` issue.

## Icons
- Real app icons read from the TV's APKs, replacing initials for apps Simple Icons doesn't cover.

## Before any Play Store listing
- Search for trademark conflicts with the name "tidytv" (fine for GitHub).

## v2 features researched 2026-10-20 (not started)

### Remote control
- Hybrid. ADB baseline (already connected, the only open route on Fire TV), ideally scrcpy-server in
  control-only mode (Apache-2.0, millisecond key/text/touch injection). Plain `input keyevent` is a
  fallback: 50–150 ms on Android 12+, 300–1000 ms on older TVs and Fire OS.
- Optional Android TV Remote Protocol v2 (TLS, pairing on 6467 with a 6-hex code, session on 6466) on
  Google/Android TV: ~10–30 ms keys, no developer mode, wake from standby, proper IME text.
  Write it fresh in Kotlin from tronikos/androidtvremote2 (Apache-2.0, protos) and AOSP
  google-tv-pairing-protocol (Apache-2.0). Generate our own client cert.
- harimoradiya/androidtvremote (Apache-2.0, Compose): study and copy UI. NEVER copy its
  `ssl/KeyStoreManager.java` (decompiled from a commercial app), the Amazon jars or `WhisperPlay.jar.cache`.
  outofcontextapps/androidtvremote is the same code relabelled MIT; use only its `.proto` files.
- GPL, ideas only: ADBKeyBoard, HA-Firemote, erenseymen/android-tv-remote.
- Size: ADB D-pad/home/back/volume ~600–900 LOC; v2 transport +~1,500 LOC; keyboard and app grid ~2–3 days each.

### File transfer and sideloading
- Kadb 2.1.5 already has push/pull (Source/Sink), sync `listV2/statV2`, `install(Source,size)`,
  `installMultiple`, `uninstall`, `execCmd`, `abbExec`. Progress: wrap the Source in a counting okio
  ForwardingSource.
- Split APKs: `install-create -S` → `install-write -S <size> <id> <name> -` streamed → `install-commit`,
  `install-abandon` on failure. Fallback for hanging streamed installs (Android 14, Fire OS): push to
  /data/local/tmp then `pm install`. OBB to /sdcard/Android/obb/<pkg>/.
- Copy from sam1am/anyapk (README says Apache-2.0 but no LICENSE file; ask the author to add one):
  `AdbInstaller.installMultiple`, `PackageBundle.kt` (.apks/.xapk/.apkm). Pick splits for the TV's
  ABI/density (`ro.product.cpu.abilist`, `wm density`), not the phone's. Alternative:
  solrudev/Ackpine `ackpine-splits` (Apache-2.0).
- "Send a phone app to the TV": `ApplicationInfo.sourceDir` + `splitSourceDirs`. Warn on ABI mismatch,
  no leanback entry (launch via `monkey -p`), touchscreen-only apps.
- GPL, ideas only: aShellYou (good remote file-browser UX), App Manager, SAI.
- Size: send file ~450 LOC; browse TV files ~700; APK/split sideload ~1,000; send phone app ~400.
