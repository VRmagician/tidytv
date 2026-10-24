# What a real TCL G06 has on it, and what TidyTV could do about it (24 Oct 2026)

**The TV:** TCL G06, product `G06_2K_GB`, Google TV on Android 11, 1.4 GB RAM, 10 GB storage. It was read over adb, read-only, except for one reversible test on Print services. TidyTV's own code (`TvReader`, `ModelMatcher`, `Grouping`, `Protected`) was run against the live TV.

## The inventory

- **132 packages:** 122 preinstalled and 10 installed by the owner (Stremio, JioHotstar, YouTube Kids, Spotify, two VPNs, a file explorer, a send-files app, AT4K Launcher, a video manager). None are turned off.
- **Memory:** 29 processes use 992 MB of 1,463 MB. The top users:
  - YouTube, 197 MB (it was open)
  - the Google TV home screen (launcherx), 151 MB
  - Play Store, 101 MB
  - Google Assistant (katniss), 83 MB
  - Play Services, 80 MB
  - Ambient mode (dreamx), 72 MB
  - Chromecast built-in, 42 MB
  - TV Settings, 42 MB
  - TCL ttvs (tracking and the updater), 23 MB
  - TCL tv, 18 MB
  - TCL guard, 14 MB
  - TCL usercenter, 13 MB
  - TCL dashboard, 11 MB
- **Storage:** 5.7 GB free of 10 GB. Apps hold 1.9 GB of code, 2.0 GB of data and **1.5 GB of cache**: Stremio has 1.08 GB, launcherx 167 MB, Hotstar 125 MB and TCL's ad "waterfall" 68 MB.

## What TidyTV does with each package

From its own code, with the G07 list (the closest match, 26 of 34 apps present):

| Outcome | Count | Examples |
|---|---|---|
| Offered, ticked | 13 | Ambient mode, TCL guard, TCL usercenter, TCL message box, TCL esticker, waterfall ads, partner customizer, Google Feedback, partner setup, hotel menu |
| Offered, not ticked | 7 | TCL dashboard, Prime Video, Play Games, Calendar sync, TCL webview, T-Solo, Media Center |
| "Show more apps" only | 22 | Print services, CTS shims, backup helpers, HTML viewer, owner apps (Stremio, Hotstar, YouTube Kids, file explorer) |
| Mentioned in other lists, never offered | 10 | YouTube, Netflix, Spotify, TalkBack, Google TTS, TCL smart Alexa, TCL copy-data-to-TV |
| On no list at all | 18 | Dolby audio, TCL shortcut menu, overlays, captive portal, both VPNs, AT4K Launcher |
| Home apps (only the home-screen step touches them) | 2 | launcherx, setupwraith |
| Protected | 59 | Android core, Play Store/Services, keyboard, TV input, the remote, TCL tv/settings/system server, and owner-choice keeps from the list: Assistant, Chromecast, YouTube Music, TCL ttvs |

**Why the owner saw only Print services:** the phone ran 0.6.0, the build on Play then. It matched this "Smart TV" to an Amazon Fire TV list. 0.6.1 and 0.7.0 fix that, and offer 20 apps here.

## Removal methods this firmware allows (tested on Print services, then put back)

| Method | Result | Undo |
|---|---|---|
| `pm disable-user --user 0` | works | `pm enable` |
| `pm uninstall -k --user 0` | works; the app is gone for the owner's user | `cmd package install-existing` brings it back (the APK stays on /system) |
| `pm suspend` | works | `pm unsuspend` |
| `pm hide` | refused (needs root) | n/a |

The TV also has users 10 and 11, probably TCL profiles. An uninstall for user 0 doesn't touch them.

## What this suggests for the app

1. **Real names.** Labels like "Tcl guard", "Vending" and "Gms" aren't helpful. The app already pulls each APK to get its icon (`ApkIcons`); `PackageManager.getPackageArchiveInfo(...).applicationInfo.loadLabel` on the same file gives the real name.
2. **An "All apps" view** next to the recommended list. Every installed app in tiers: Recommended / Your call / Seen on other TVs / Not on any list, so look first / Protected (locked, with the reason). Each one gets badges: RAM used now, starts at boot, uses the internet, preinstalled or yours, and storage.
3. **Uninstall as a second removal method**:
   - **Preinstalled apps:** "Remove for this TV" (`pm uninstall -k --user 0`), logged in the action log. Restore runs `install-existing`.
   - **Apps the owner installed:** a normal uninstall, clearly marked as permanent.

   Turning off stays the default; Uninstall is an explicit choice per app.

   Note: TCL's ttvs turns some apps back on at every boot, even after an uninstall for user 0 (upstream tcl-65c735 notes). Check my TV already catches that.
4. **Free up space:** `pm trim-caches` frees caches safely, about 1.5 GB here. Ad apps' data, like waterfall's 141 MB, could go with a per-app clear.
5. **Memory view:** what's using RAM right now, so people see why the TV is slow. There's also a softer "stop in background" option per app (`cmd appops set <pkg> RUN_ANY_IN_BACKGROUND ignore`). It isn't tested here yet.
6. **Show more apps** always as a collapsed section, not only when a list finds fewer than five.
7. **Owner-choice apps with real costs:** Assistant (83 MB, and the mic button stops), Chromecast (42 MB, and casting stops), TCL ttvs (23 MB of tracking, but it's also the firmware updater). These should be visible "Your call" items with the trade-off in one line, not hidden as protected.
