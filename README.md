# tidytv

**Download:** [tidytv.winkle.dev](https://tidytv.winkle.dev) · [GitHub releases](https://github.com/jolan94/tidytv/releases/latest)

A free, open-source Android **phone** app that cleans up an Android TV or Google TV over Wi-Fi.
No computer and no commands to type. You tap through a few screens; the phone connects to the TV
over ADB, saves a backup, turns bloat off (it never uninstalls anything), can add a small clean
home screen, and puts everything back with one button.

## How it works

1. **What TV do you have?** Android TV / Google TV, Fire TV (clean-up only) or "not sure".
   Samsung and LG aren't supported: they don't run Android.
2. **Get ready**: turn on developer options and Network (or USB) debugging on the TV.
3. **Connect**: tidytv finds the TV on your Wi-Fi. Most TVs only ask "Allow debugging?" once.
   TVs that only offer Wireless debugging ask for the 6-digit code shown on screen.
4. **Review**: a backup is saved first, then apps are shown in groups (Ads & tracking, Preloaded
   apps, Voice assistant, Casting, Updates, Streaming). The safe groups start switched on.
5. **Tidying**: each app is turned off and checked, one at a time. Anything unexpected stops the run.
   Then, optionally, a cleaner home screen.
6. **Done**: memory freed, how to turn debugging off again, Restore everything, and Share logs.

## Safety rules

- Turns apps off with `pm disable-user`; never uninstalls.
- A backup is taken before any change and can't be skipped.
- Play Store, Play Services, keyboards, TV inputs, remote and Bluetooth pairing, Settings and setup
  apps are never touched, whatever a list says, checked again right before every command.
- Every change is written to an undo list on the TV and an append-only log on the phone *before* it
  is made, so Restore works even if the phone is lost or the Wi-Fi drops mid-run.
- The new home screen only takes over once it has started; if Home lands anywhere else, tidytv
  rolls back.

## Building

```sh
git clone --recurse-submodules https://github.com/jolan94/tidytv
./gradlew test                         # all logic tests, no TV needed
./gradlew :app:verifyRoborazziDebug    # screenshot tests
./gradlew :app:assembleDebug           # app/build/outputs/apk/debug/app-debug.apk
```

Debug builds list a **Demo TV** on the connect screen: a simulated Google TV for trying the whole
flow without a real one.

| Module | What's in it |
|---|---|
| `core` | Pure JVM: list parser, matcher, grouping, protected set, backup, Debloater, LauncherHandover, Restorer, action log, connect and wizard state machines, and `FakeTv` |
| `adb` | Kadb-backed connection, pairing and subnet scan, tested against `FakeAdbd` |
| `app` | Compose UI (concept C), view model, NSD discovery, log sharing |
| `upstream/` | Submodules: cleanyourtv (lists) and tv-detox (home screen) |

## Found a problem?

Tap **Share logs** in the app and send the file with your TV model. Logs carry no personal data:
serial numbers, Wi-Fi names, MAC addresses and IP addresses are removed first. Each real-TV log
becomes a new test fixture.

See [docs/ROADMAP.md](docs/ROADMAP.md) for what's planned next, and [NOTICE](NOTICE) for credits.
