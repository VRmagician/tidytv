<div align="center">

<img src="assets/brand/icons/a1-mascot-flat.png" width="110" alt="TidyTV icon: a smiling blue TV">

# TidyTV

**Your TV, without the junk.**<br>
A free phone app that turns off the ads, trackers and bloat on Android TV and Google TV.<br>
No computer. No AI. One tap puts it all back.

[![Download the APK](https://img.shields.io/badge/Download-free%20APK-ffd54a?style=for-the-badge&labelColor=1b1b3a)](https://tidytv.winkle.dev)
[![Latest release](https://img.shields.io/github/v/release/jolan94/tidytv?style=for-the-badge&color=2f6bff&labelColor=1b1b3a)](https://github.com/jolan94/tidytv/releases/latest)
[![Star on GitHub](https://img.shields.io/github/stars/jolan94/tidytv?style=for-the-badge&color=ffffff&labelColor=1b1b3a&logo=github)](https://github.com/jolan94/tidytv/stargazers)

<a href="https://tidytv.winkle.dev/#video"><img src="docs/media/video-poster.png" width="820" alt="Watch the 30-second TidyTV video"></a>

<sub>▶ Watch the 30-second video · Android 8 or newer · MIT licence</sub>

</div>

---

## Before and after

<img src="docs/media/before-after.png" alt="A Google TV home screen full of sponsored rows, then the same TV with a clean home screen after TidyTV">

TidyTV connects to your TV from your phone over Wi-Fi, saves a backup, and switches off the apps you
don't want. It never deletes anything, and **Restore everything** turns it all back on.

## How it works

<table>
<tr>
<td align="center" width="25%"><img src="docs/media/screens/01-welcome.png" width="190" alt="Welcome screen"><br><b>1. Pick your TV</b><br><sub>Android TV, Google TV or Fire TV</sub></td>
<td align="center" width="25%"><img src="docs/media/screens/04-press-allow.png" width="190" alt="Press Allow on your TV"><br><b>2. Connect</b><br><sub>Same Wi-Fi. Press Allow once.</sub></td>
<td align="center" width="25%"><img src="docs/media/screens/05-review.png" width="190" alt="Backup saved, then the apps in groups"><br><b>3. Backup, then pick</b><br><sub>Every app has a switch</sub></td>
<td align="center" width="25%"><img src="docs/media/screens/06-tidying.png" width="190" alt="Apps being turned off one by one"><br><b>4. Tidy</b><br><sub>One app at a time, each one checked</sub></td>
</tr>
<tr>
<td align="center"><img src="docs/media/screens/08-styled-pick.png" width="190" alt="Choose a clean home screen"><br><b>5. Clean home screen</b><br><sub>Optional: Simple or Apple TV style</sub></td>
<td align="center"><img src="docs/media/screens/09-home-on.png" width="190" alt="New home screen is on"><br><b>6. Done</b><br><sub>Rolls back if Home doesn't land</sub></td>
<td align="center"><img src="docs/media/screens/11-share-card.png" width="190" alt="Share card"><br><b>7. Share it</b><br><sub>A card with what you tidied</sub></td>
<td align="center"><img src="docs/media/screens/10-done.png" width="190" alt="Done screen with Restore everything"><br><b>8. Undo anytime</b><br><sub>Restore everything, one tap</sub></td>
</tr>
</table>

## Why it's safe

| | |
|---|---|
| 🧷 **Backup first, every time** | Apps, settings and home screen are saved before any change. It can't be skipped. |
| 🔕 **Turned off, never deleted** | Uses Android's own `pm disable-user`. Nothing is uninstalled. |
| 🛡️ **Protected apps stay on** | Play Store, Play Services, keyboards, TV inputs, the remote, Bluetooth, Settings and setup apps are never touched, checked again before every command. |
| 📺 **Tested lists for 93 models** | From [cleanyourtv](https://github.com/Gathin23/cleanyourtv), each tried on a real TV. [See yours →](https://tidytv.winkle.dev/tv/) |
| ↩️ **Undo survives anything** | Every change is logged on the TV and the phone *before* it happens, so Restore works even after a Wi-Fi drop or a lost phone. |

## Works with

✅ Android TV and Google TV sets and boxes (Sony, TCL, Xiaomi, Hisense, Philips, Chromecast, NVIDIA Shield…)<br>
🟧 Fire TV: clean-up only, Amazon's home screen stays<br>
❌ Samsung (Tizen) and LG (webOS): they don't run Android

Guides: [cleaning up a TV with Claude or another AI](https://tidytv.winkle.dev/guides/claude-code-google-tv/) ·
[which apps are safe to turn off](https://tidytv.winkle.dev/guides/safe-to-disable-android-tv-apps/) ·
[all guides](https://tidytv.winkle.dev/guides/)

## Privacy

TidyTV talks only to your TV, over your own Wi-Fi. It has no account, no analytics and no ads. It
sends something over the internet only when you choose to: tapping **Send** in the feedback form
(your message, phone model, Android and app version, and the log or screenshot if you tick them), or
sharing your result card or logs through Android's share sheet.

## Found a problem?

Tap **Report a problem** in the app (it can attach the log), or **Share logs** and send the file with
your TV model. Logs carry no personal data: serial numbers, Wi-Fi names, MAC addresses and IP
addresses are removed first. Each real-TV log becomes a new test fixture.

If TidyTV helped, a ⭐ on this repo is how other people find it.

<details>
<summary><b>Building from source</b></summary>

```sh
git clone --recurse-submodules https://github.com/jolan94/tidytv
./gradlew test                         # all logic tests, no TV needed
./gradlew :app:verifyRoborazziDebug    # screenshot tests
./gradlew :app:assembleDebug           # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease         # signed with keys/signing.properties if present, else the debug key
tools/build-styled-home.sh             # rebuild the bundled Apple TV style home screen from upstream
python3 tools/build-pages.py           # site/tv/<model>/ pages, site/guides/, sitemap, main-page TV search
```

Debug builds list a **Demo TV** on the connect screen: a simulated Google TV for trying the whole
flow without a real one. `tools/demo/` has the helpers used to record it.

The site's model pages come from the cleanyourtv lists (the facts) plus `content/tv/<id>.json` (a short
plain-English intro and FAQ, written from each list's own header). Guides are Markdown in `content/guides/`.
`python3 tools/build-pages.py --data` also writes `content/tv/data/` for whoever writes new intros.
The promo video is a [HyperFrames](https://hyperframes.heygen.com) project in `videos/tidytv-promo/film/`.

The package name is `com.appfactory.tidytv` from 0.2.0 on. Version 0.1.0 used `dev.tidytv.app`,
so uninstall 0.1.0 before installing 0.2.0. Release keys live in `keys/`, which git ignores.

| Module | What's in it |
|---|---|
| `core` | Pure JVM: list parser, matcher, grouping, protected set, backup, Debloater, LauncherHandover, Restorer, action log, connect and wizard state machines, and `FakeTv` |
| `adb` | Kadb-backed connection, pairing and subnet scan, tested against `FakeAdbd` |
| `app` | Compose UI, view model, NSD discovery, feedback, share card, log sharing |
| `upstream/` | Submodules: cleanyourtv (lists and the Apple TV style home screen) and tv-detox (simple home screen) |

</details>

---

<div align="center">
<sub>Lists by <a href="https://github.com/Gathin23/cleanyourtv">cleanyourtv</a> · Simple home screen by <a href="https://github.com/Ansub/tv-detox">tv-detox</a> · <a href="docs/ROADMAP.md">Roadmap</a> · <a href="NOTICE">Credits</a><br>
Not affiliated with Google, Amazon or any TV maker. The TV screens shown are illustrations.</sub>
</div>
