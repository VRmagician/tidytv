# TidyTV

**Download:** [tidytv.winkle.dev](https://tidytv.winkle.dev) · [GitHub releases](https://github.com/jolan94/tidytv/releases/latest)

A free, open-source Android **phone** app that cleans up an Android TV or Google TV over Wi-Fi.
No computer and no commands to type. You tap through a few screens; the phone connects to the TV
over ADB, saves a backup, turns bloat off (it never uninstalls anything), can add a small clean
home screen, and puts everything back with one button.

## How it works

1. **What TV do you have?** Android TV / Google TV, Fire TV (clean-up only) or "not sure".
   Samsung and LG aren't supported: they don't run Android.
2. **Get ready**: turn on developer options and Network (or USB) debugging on the TV.
3. **Connect**: TidyTV finds the TV on your Wi-Fi. Most TVs only ask "Allow debugging?" once.
   TVs that only offer Wireless debugging ask for the 6-digit code shown on screen.
4. **Review**: a backup is saved first, then apps are shown in groups (Ads & tracking, Preloaded
   apps, Voice assistant, Casting, Updates, Streaming). The safe groups start switched on.
5. **Tidying**: each app is turned off and checked, one at a time. Anything unexpected stops the run.
   Then, optionally, a cleaner home screen: **Simple** (tv-detox, the default, any Android TV) or
   **Apple TV style** (cleanyourtv, Android 8 and newer).
6. **Done**: memory freed, how to turn debugging off again, Restore everything, and Share logs.

## Safety rules

- Turns apps off with `pm disable-user`; never uninstalls.
- A backup is taken before any change and can't be skipped.
- Play Store, Play Services, keyboards, TV inputs, remote and Bluetooth pairing, Settings and setup
  apps are never touched, whatever a list says, checked again right before every command.
- Every change is written to an undo list on the TV and an append-only log on the phone *before* it
  is made, so Restore works even if the phone is lost or the Wi-Fi drops mid-run.
- The new home screen only takes over once it has started; if Home lands anywhere else, TidyTV
  rolls back.

Apps in the review list show their real icons, read on the phone from each app's APK on the TV
(apps over 12 MB fall back to a brand mark or initials).

## Privacy

TidyTV talks only to your TV, over your own Wi-Fi. It has no account, no analytics and no ads. It
sends something over the internet only when you choose to: tapping **Send** in the feedback form
(your message, phone model, Android and app version, and the log or screenshot if you tick them), or
sharing your result card or logs through Android's share sheet.

## Building

```sh
git clone --recurse-submodules https://github.com/jolan94/tidytv
./gradlew test                         # all logic tests, no TV needed
./gradlew :app:verifyRoborazziDebug    # screenshot tests
./gradlew :app:assembleDebug           # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease         # signed with keys/signing.properties if present, else the debug key
tools/build-styled-home.sh             # rebuild the bundled Apple TV style home screen from upstream
python3 tools/build-pages.py           # site/tv/<model>/ pages, site/guides/, sitemap, main-page TV search
```

The site's model pages come from the cleanyourtv lists (the facts) plus `content/tv/<id>.json` (a short
plain-English intro and FAQ, written from each list's own header). Guides are Markdown in `content/guides/`.
`python3 tools/build-pages.py --data` also writes `content/tv/data/` for whoever writes new intros.

The package name is `com.appfactory.tidytv` from 0.2.0 on. Version 0.1.0 used `dev.tidytv.app`,
so uninstall 0.1.0 before installing 0.2.0. Release keys live in `keys/`, which git ignores.

Debug builds list a **Demo TV** on the connect screen: a simulated Google TV for trying the whole
flow without a real one.

| Module | What's in it |
|---|---|
| `core` | Pure JVM: list parser, matcher, grouping, protected set, backup, Debloater, LauncherHandover, Restorer, action log, connect and wizard state machines, and `FakeTv` |
| `adb` | Kadb-backed connection, pairing and subnet scan, tested against `FakeAdbd` |
| `app` | Compose UI (concept C), view model, NSD discovery, log sharing |
| `upstream/` | Submodules: cleanyourtv (lists and the Apple TV style home screen) and tv-detox (simple home screen) |

## Found a problem?

Tap **Share logs** in the app and send the file with your TV model. Logs carry no personal data:
serial numbers, Wi-Fi names, MAC addresses and IP addresses are removed first. Each real-TV log
becomes a new test fixture.

See [docs/ROADMAP.md](docs/ROADMAP.md) for what's planned next, and [NOTICE](NOTICE) for credits.
