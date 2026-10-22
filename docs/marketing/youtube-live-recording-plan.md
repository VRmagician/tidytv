# YouTube: real TV clean-up, recorded live

Goal: one real, unedited-feeling clean-up of a real TV with TidyTV, shown as phone screen + TV side
by side. The README and site say "the TV screens shown are illustrations", so real footage is the
proof those pages are missing.

## What to publish

| Piece | Length | Format | Job |
|---|---|---|---|
| Main video | 6–9 min | 16:9, 1080p or 4K | The search result: "how to remove ads from Google TV home screen" |
| Short | 30–50 s | 9:16 | Before → tap → after. Links to the main video |
| Site embed | same as main | — | Replace or sit beside the promo on tidytv.winkle.dev/#video |

## Before recording day

**The TV**
- Pick a TV with an obviously cluttered Google TV home (sponsored rows, "Free to watch", top ad
  banner). The uglier the before, the better the after.
- If TidyTV has already been used on it: Done screen → **Restore everything**, then reboot, so the
  ads are back.
- Remove the phone's saved authorisation so the **Allow** prompt appears on camera: TV Settings →
  Developer options → Revoke USB debugging authorisations. Decide now whether the video shows turning
  on Developer options (recommended: yes, it's the step viewers get stuck on; keep it to ~40 s).
- Clean the screen. Turn off motion smoothing and set picture to Standard (cameras hate Vivid).
- Check the TV home screen for things you don't want public: account avatar/name, kids profiles,
  "continue watching" history. Switch to a spare Google account if needed.

**The phone**
- Install the release build (not debug, so no Demo TV entry shows).
- Do Not Disturb on, battery above 80 %, clear the status bar of odd icons.
- Developer options → **Show taps** on, so viewers can see where you press.
- Use a neutral wallpaper; close other apps so the recents screen is clean.
- Check what the connect screen shows: IP address and TV name are fine to show, but no Wi-Fi name
  you'd rather keep private (blur it in the edit if so).

**Dry run (the day before)**
- Do the whole flow once on the same TV, note timings for each screen, then Restore everything and
  reboot. This catches a TV-specific surprise before the camera is rolling.
- Note which list TidyTV matches for this model, and how many apps it turns off. That number is the
  title hook ("I turned off 37 apps on my TV").

## Recording setup

Two sources, recorded at once, synced in the edit:

1. **Phone screen**: Android's built-in screen recorder (Quick Settings → Screen record, no audio,
   "show touches" on). 60 fps if available.
2. **TV**: a second phone or camera on a tripod, slightly off-axis to avoid your reflection, lights
   off behind the camera, exposure and focus locked on the TV (tap-and-hold to lock). 4K/30 is fine.
   Frame so the remote and your hand can come into shot.

Sync: start both, then press a button on the phone that changes the TV (or clap on camera and tap the
phone at the same moment). Leave both running for the whole flow; cut later.

Optional third angle: over-the-shoulder phone-in-hand shot for the intro and the reveal.

Audio: record the voice-over separately afterwards. A live mic picks up TV and room noise and you
won't want to redo a take because of a stumble.

## Shot list / script (main video)

| # | Time | Shows | Says (roughly) |
|---|---|---|---|
| 1 | 0:00–0:15 | TV: scroll the cluttered home, ads autoplaying | "This is my TV. Half of it is ads I never asked for. Let's fix it from my phone, no computer." |
| 2 | 0:15–0:30 | The after, flashed for 2 s, then back | "This is where we're going. And one tap undoes all of it." |
| 3 | 0:30–1:15 | TV: Settings → About → tap Build 7 times → Developer options → Network/USB debugging on | "One-time setup on the TV." |
| 4 | 1:15–2:00 | Split: phone picks TV type, finds the TV; TV shows **Allow** prompt, you press it | "Same Wi-Fi. Press Allow once." |
| 5 | 2:00–3:00 | Phone: backup saved, apps in groups, scroll the switches | Explain backup first, nothing deleted, protected apps stay on |
| 6 | 3:00–4:30 | Split: Tidy running app by app; TV in the background | Speed up the middle 2–4×, keep the first and last few real-time |
| 7 | 4:30–5:30 | Phone: choose Simple or Apple TV style home; TV switches | "Optional: a clean home screen." |
| 8 | 5:30–6:30 | TV: the new home, open YouTube/Netflix to show they still work | Answer the obvious worry: "My apps still work." |
| 9 | 6:30–7:15 | Phone: Done screen, **Restore everything** button (show it, don't press, or press it and show the ads coming back) | "If you change your mind, one tap." |
| 10 | 7:15–7:45 | Before/after side by side, download link on screen | "Free, open source, link below. Tell me your TV model in the comments." |

Short: shots 1 → 4 (Allow press) → 6 (sped up) → 8, with captions, no Developer-options section.

## Edit

- Split-screen layout: TV on the left (or top in the Short), phone on the right in a phone frame.
- Captions burned in for the Short; uploaded SRT for the main video.
- Zoom in on the TV Allow prompt and on the app count.
- Keep real waiting visible but short: a 2× speed-up with a small "2×" badge is honest; jump cuts that
  hide failures are not.
- If anything goes wrong on the take (a retry, a slow connect), keep it if the app recovers: it shows
  the safety story working.
- The promo's HyperFrames project in `videos/tidytv-promo/film/` can supply the intro card, end card
  and the brand font.

## Upload

**Title options** (pick by the TV you use):
- How to remove ads from your Google TV home screen (no computer)
- I removed every ad from my [Brand] Google TV, from my phone
- Clean up Android TV bloatware in 5 minutes, free and undoable

**Thumbnail**: real before/after of the same TV, split down the middle, the mascot, 3–4 words max
("NO MORE TV ADS").

**Description**: one-line summary, download link (tidytv.winkle.dev), GitHub link, supported TVs
(Android TV / Google TV yes, Fire TV clean-up only, Samsung/LG no), "it turns apps off, never deletes,
Restore everything undoes it", credits to cleanyourtv and tv-detox, chapters:

```
0:00 My TV before
0:30 Turn on developer options
1:15 Connect the phone
2:00 Backup and pick apps
3:00 Tidy
4:30 New home screen
5:30 Do my apps still work?
6:30 Undo everything
```

**Pinned comment**: "Which TV do you have? I'll check it's on the tested list." Model questions
become new model pages and new FakeTv fixtures.

After upload: embed on the site and README, link it from the model page of the TV you used, and post
the Short.

## Checklist for the day

- [ ] TV restored, rebooted, authorisations revoked, ads visible
- [ ] Phone: DND, Show taps, release build, battery charged
- [ ] Camera: tripod, exposure locked, reflection checked, storage free
- [ ] Both recordings running, sync tap done
- [ ] Full flow recorded in one go
- [ ] Extra B-roll: home before, home after, apps opening, remote in hand
- [ ] Before you leave the TV: decide whether it stays tidied or gets restored
